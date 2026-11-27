package vip.mystery0.xhu.timetable.nativeui

import androidx.lifecycle.viewModelScope
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.utils.now
import kotlinx.serialization.json.*
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.FeatureString
import vip.mystery0.xhu.timetable.config.Feature as AppFeature
import vip.mystery0.xhu.timetable.config.store.*
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.feature.FeatureHub
import vip.mystery0.xhu.timetable.initFeature
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.module.HELP_URL
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.platform.ArkTsBridge
import vip.mystery0.xhu.timetable.repository.JobRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerProfileViewModel
import vip.mystery0.xhu.timetable.widget.WidgetRefreshSignal
import vip.mystery0.xhu.timetable.widget.WidgetSnapshot
import vip.mystery0.xhu.timetable.widget.WidgetSnapshotRepo
import kotlin.time.Clock

object NativeCore : KoinComponent {
    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var generation = 0
    private var running = false
    private var runtimeScope: CoroutineScope? = null
    private var mainModel: MainViewModel? = null
    private var homeDate: LocalDate? = null
    private var loginModel: LoginViewModel? = null
    private var homeObserver: Job? = null
    private var profileModel: PagerProfileViewModel? = null
    private var profileObserver: Job? = null
    private val requests = mutableMapOf<String, Job>()
    private val loginMutex = Mutex()
    private val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val widgetSyncRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @OptIn(FlowPreview::class)
    fun start() {
        dispose()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        running = true
        scope.launch {
            changes.debounce(80).collect {
                ArkTsBridge.call("nativeMessage", "{\"id\":\"\",\"action\":\"changed\"}")
            }
        }
        scope.launch {
            widgetSyncRequests.debounce(250).collect {
                syncWidgetsNow()
            }
        }
        scope.launch {
            WidgetRefreshSignal.changes.drop(1).collect {
                scheduleWidgetSync()
            }
        }
        scope.launch {
            EventBus.flow.collect { event ->
                when (event.peekContent()) {
                    EventType.CHANGE_TERM_START_TIME, EventType.CHANGE_SHOW_CUSTOM_COURSE,
                    EventType.CHANGE_SHOW_CUSTOM_THING, EventType.CHANGE_COURSE_COLOR,
                    EventType.CHANGE_CUSTOM_UI, EventType.CHANGE_CURRENT_YEAR_AND_TERM,
                    EventType.CHANGE_SHOW_NOT_THIS_WEEK, EventType.CHANGE_SHOW_STATUS -> {
                        reloadHome()
                        changed()
                    }
                    EventType.USER_LIST_CHANGED, EventType.CHANGE_MAIN_USER,
                    EventType.MAIN_USER_LOGOUT -> changed()
                    EventType.UPDATE_NOTICE_CHECK -> profileModel?.checkUnReadNotice()
                    EventType.UPDATE_FEEDBACK_CHECK -> profileModel?.checkUnReadFeedback()
                    else -> Unit
                }
            }
        }
    }

    fun dispatch(request: NativeRequest) {
        check(running) { "应用尚未初始化" }
        check(request.id !in requests) { "请求标识重复" }
        val currentGeneration = generation
        val job = scope.launch(start = CoroutineStart.LAZY) {
            val response = try {
                val timeout = if (request.action in setOf("avatar", "pickBackground")) 180_000L else 25_000L
                val data = withTimeout(timeout) { execute(request.action, request.payload) }
                buildJsonObject { put("id", request.id); put("data", data); put("error", "") }
            } catch (_: TimeoutCancellationException) {
                if (request.action == "login") {
                    loginModel?.viewModelScope?.cancel()
                    loginModel = null
                }
                errorResponse(request.id, "操作超时，请检查网络后重试")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = when (request.action) {
                    "autoScoreStatus" -> "获取成绩提醒状态失败，请刷新重试"
                    "stopAutoScore" -> "取消结果未确认，请刷新状态"
                    else -> error.desc().take(500)
                }
                errorResponse(request.id, message)
            } finally {
                requests.remove(request.id)
            }
            if (generation == currentGeneration && running) {
                ArkTsBridge.call("nativeMessage", response.toString())
            }
        }
        requests[request.id] = job
        job.start()
    }

    private suspend fun execute(action: String, params: JsonObject): JsonElement {
        when (action) {
            "bootstrap" -> return bootstrap()
            "privacy" -> {
                require(params.flag("accepted")) { "请先阅读并同意隐私政策" }
                setCacheStore { allowPrivacy = true }
                startRuntime()
                return bootstrap()
            }
            "openUrl" -> {
                ArkTsBridge.call("openUrl", params.text("url"))
                return JsonNull
            }
        }
        check(getCacheStore { allowPrivacy }) { "请先阅读并同意隐私政策" }
        startRuntime()
        when (action) {
            "login" -> return login(params)
            "loginHelpDismiss" -> { loginModel?.dismissHelpDialog(); return JsonNull }
            "copyText" -> { ArkTsBridge.call("copyText", params.text("text")); return JsonNull }
            "share" -> { ArkTsBridge.call("shareText", params.text("text")); return JsonNull }
            "feedbackClose" -> { closeNativeFeedback(); return JsonNull }
            "query" -> if (params.text("kind") in setOf("licenses", "sponsors")) return NativeQueries.query(params)
            "termStartDecision" -> {
                if (params.flag("sync")) {
                    setConfigStore { customTermStartDate = Customisable.clearCustom(LocalDate.MIN) }
                }
                StartRepo.pendingTermStartChangeAlert = null
                reloadHome()
                changed()
                return JsonNull
            }
        }
        check(UserStore.isLogin()) { "请先登录账号" }
        return when (action) {
            "home" -> homeSnapshot(home())
            "resumeHome" -> {
                val today = LocalDate.now()
                val showTomorrow = getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it } ?: false
                if (mainModel != null && (homeDate != today || mainModel?.showTomorrowCourse?.value != showTomorrow)) {
                    homeDate = today
                    reloadHome()
                }
                JsonNull
            }
            "clearHomeMessage" -> {
                val model = home()
                if (model.errorMessage.value == params.text("text")) model.errorMessage.value = ""
                JsonNull
            }
            "refresh" -> {
                home().errorMessage.value = ""
                home().refreshCloudDataToState()
                JsonNull
            }
            "todayDate" -> {
                home().selectTodayDate(LocalDate.parse(params.text("date")))
                JsonNull
            }
            "week" -> {
                val week = params.number("week")
                require(week in 1..20) { "请选择第1至20周" }
                home().changeCurrentWeek(week)
                JsonNull
            }
            "options" -> NativeQueries.options(params)
            "query" -> NativeQueries.query(params)
            "autoScoreStatus" -> nativeJson.encodeToJsonElement(JobRepo.fetchAutoScoreJobStatus())
            "stopAutoScore" -> {
                JobRepo.stopAutoScoreJob()
                JsonNull
            }
            "feedbackOpen", "feedbackReconnect" -> {
                connectNativeFeedback()
                nativeFeedbackStatus()
            }
            "feedbackStatus" -> nativeFeedbackStatus()
            "editor" -> NativeQueries.editor(params)
            "save", "delete" -> {
                val result = if (action == "save") NativeQueries.save(params) else NativeQueries.delete(params)
                if (params.text("kind") != "courseColors") home().refreshCloudDataToState()
                changed()
                result
            }
            "feedbackSend" -> NativeQueries.feedbackSend(params).also { changed() }
            "urge" -> NativeQueries.urge(params)
            "settings" -> NativeSettings.snapshot(params.text("kind", "settings"))
            "saveAppearance" -> {
                NativeSettings.saveAppearance(params["values"] as? JsonObject ?: error("界面设置内容无效"))
                reloadHome()
                changed()
                JsonNull
            }
            "shareSchoolCalendar" -> {
                NativeSettings.shareSchoolCalendar(params.text("url"))
                JsonNull
            }
            "setSetting" -> {
                val key = params.text("key")
                NativeSettings.set(key, params.text("value"))
                if (key == "debugMode") return JsonNull
                if (key == "revokePrivacy") {
                    stopRuntime()
                    changed()
                    return JsonNull
                }
                if (key in setOf("disablePoems", "showPoemsTranslate", "resetPoemsToken")) mainModel?.reloadPoems()
                reloadHome()
                if (key in setOf("yearTerm", "campus", "multiAccountMode", "showCustomCourseOnWeek", "showCustomThing")) {
                    home().refreshCloudDataToState()
                }
                changed()
                JsonNull
            }
            "account" -> account(params)
            "avatar" -> avatar(params)
            "pickBackground" -> {
                val path = ArkTsBridge.call("pickImage", "")
                if (path.isNotBlank()) {
                    setConfigStore { backgroundImage = PlatformFile(path) }
                    changed()
                }
                JsonNull
            }
            else -> error("不支持此操作")
        }
    }

    private suspend fun bootstrap(): JsonObject {
        val privacy = getCacheStore { allowPrivacy }
        if (privacy) startRuntime()
        scheduleWidgetSync()
        val user = if (privacy) UserStore.getMainUser() else null
        val avatar = getConfigStore { customAvatar }.ifBlank {
            val suffix = (user?.info?.name.orEmpty().md5().take(2).toInt(16) % 4) + 1
            "builtin:${if (user?.info?.gender == Gender.FEMALE) "girl" else "boy"}$suffix"
        }
        val profile = if (privacy && user != null) profile() else null
        return buildJsonObject {
            put("privacy", privacy)
            put("loggedIn", user != null)
            put("version", appVersionName())
            put("nightMode", getConfigStore { nightMode.name })
            put("loginLabel", if (privacy) FeatureString.LOGIN_LABEL.getValue() else "")
            put("helpUrl", HELP_URL)
            put("privacyUrl", PRIVACY_URL)
            put("avatar", avatar)
            put("customAvatar", getConfigStore { customAvatar })
            put("users", if (privacy) userSummaries() else JsonArray(emptyList()))
            put("menuGroups", profileMenus(profile?.menu?.value.orEmpty(),
                profile?.hasUnReadNotice?.value == true, profile?.hasUnReadFeedback?.value == true))
            put("multiAccount", getConfigStore { multiAccountMode })
            put("todayAccountTemplate", getConfigStore { customAccountTitle.todayTemplate })
            put("weekAccountTemplate", getConfigStore { customAccountTitle.weekTemplate })
            StartRepo.pendingTermStartChangeAlert?.let { alert ->
                put("termAlert", buildJsonObject {
                    put("oldDate", alert.oldServerDate.toString())
                    put("newDate", alert.newServerDate.toString())
                    put("customDate", alert.customDate.toString())
                    put("message", "学校开学日期已${alert.serverChangeDescription}，当前自定义日期${alert.customDiffDescription}。是否同步学校的新日期？")
                })
            }
        }
    }

    private suspend fun startRuntime() {
        if (runtimeScope != null) return
        val runtime = CoroutineScope(scope.coroutineContext + SupervisorJob(scope.coroutineContext[Job]))
        runtimeScope = runtime
        initFeature()
        var poemsEnabled = AppFeature.JRSC.isEnabled()
        runtime.launch {
            FeatureHub.features.drop(1).collect {
                val enabled = AppFeature.JRSC.isEnabled()
                if (enabled != poemsEnabled) {
                    poemsEnabled = enabled
                    mainModel?.reloadPoems()
                }
                changed()
            }
        }
        FeatureHub.start(runtime)
        ArkTsBridge.call("setColorMode", getConfigStore { nightMode.name })
        runtime.launch {
            try {
                StartRepo.init()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // 网络初始化失败时，仍允许读取本地账号和离线课表。
            }
            reloadHome()
            changed()
        }
    }

    private suspend fun login(params: JsonObject): JsonObject = loginMutex.withLock {
        val username = params.text("username").trim()
        val password = params.text("password")
        require(username.isNotBlank()) { "请输入学号" }
        require(password.isNotBlank()) { "请输入密码" }
        val model = loginModel ?: getKoin().get<LoginViewModel>().also { loginModel = it; it.init() }
        model.login(username, password)
        val state = model.loginState.first { !it.loading }
        if (state.success) {
            resetHome()
            home().refreshCloudDataToState()
            changed()
        }
        buildJsonObject {
            put("success", state.success)
            put("error", state.errorMessage)
            put("showHelp", state.showHelpDialog)
        }
    }

    private suspend fun account(params: JsonObject): JsonElement {
        val id = params.text("id")
        val user = UserStore.getUserByStudentId(id) ?: error("账号不存在或已退出登录")
        when (params.text("operation")) {
            "main" -> UserStore.setMainUser(user)
            "logout" -> UserStore.logout(id)
            "refresh" -> {
                val info = user.withAutoLoginOnce { token -> UserRepo.reloadUserInfo(token) }
                val latestUser = UserStore.getUserByStudentId(id) ?: error("账号已退出登录")
                UserStore.updateUser(latestUser.copy(info = info))
            }
            else -> error("账号操作无效")
        }
        closeNativeFeedback()
        resetHome()
        if (UserStore.isLogin()) home().refreshCloudDataToState()
        changed()
        return JsonNull
    }

    private suspend fun avatar(params: JsonObject): JsonElement {
        val value = when (params.text("mode")) {
            "reset" -> ""
            "builtin" -> {
                val key = params.text("value").removePrefix(ProfileImages.BUILTIN_PREFIX)
                require(ProfileImages.findBuiltin(key) != null) { "头像选项无效" }
                ProfileImages.BUILTIN_PREFIX + key
            }
            "pick" -> {
                val path = ArkTsBridge.call("pickImage", "")
                if (path.isBlank()) return JsonNull
                ProfileImages.FILE_PREFIX + path
            }
            else -> error("头像操作无效")
        }
        setConfigStore { customAvatar = value }
        ProfileImages.updateCustomAvatar(value)
        changed()
        return JsonNull
    }

    private fun home(): MainViewModel {
        return mainModel ?: getKoin().get<MainViewModel>().also { model ->
            mainModel = model
            homeDate = LocalDate.now()
            homeObserver = scope.launch {
                val flows: List<Flow<*>> = listOf(
                    model.selectedTodayHeaderText, model.todayWeekDays, model.isTodayDateCustomSelected,
                    model.classTimeSlots, model.week, model.actualCurrentWeek, model.todayCourse,
                    model.todayThing, model.allWeekTables, model.calendarList, model.practicalCourseList,
                    model.loading, model.errorMessage, model.poems, model.holiday, model.emptyUser,
                    model.enableCalendarView, model.multiAccountMode, model.showTomorrowCourse,
                )
                merge(*flows.map { it.drop(1).map { Unit } }.toTypedArray()).collect { changed() }
            }
        }
    }

    private fun profile(): PagerProfileViewModel = profileModel ?: getKoin().get<PagerProfileViewModel>().also { model ->
        profileModel = model
        profileObserver = scope.launch {
            merge(model.menu.drop(1).map { Unit }, model.hasUnReadNotice.drop(1).map { Unit },
                model.hasUnReadFeedback.drop(1).map { Unit }).collect { changed() }
        }
        model.checkUnReadNotice()
        model.checkUnReadFeedback()
    }

    private fun reloadHome() {
        mainModel?.apply {
            loadConfig()
            checkMainUser()
            calculateTodayTitle()
            loadTodayHoliday()
            loadLocalDataToState()
        }
    }

    private fun resetHome() {
        homeObserver?.cancel()
        homeObserver = null
        mainModel?.viewModelScope?.cancel()
        mainModel = null
        profileObserver?.cancel()
        profileObserver = null
        profileModel?.viewModelScope?.cancel()
        profileModel = null
    }

    private fun changed() {
        changes.tryEmit(Unit)
        scheduleWidgetSync()
    }

    private fun scheduleWidgetSync() {
        widgetSyncRequests.tryEmit(Unit)
    }

    private suspend fun syncWidgetsNow() {
        if (!running) return
        try {
            val privacy = getCacheStore { allowPrivacy }
            val snapshot = if (!privacy) {
                WidgetSnapshot.empty("loggedOut", Clock.System.now().toEpochMilliseconds())
            } else {
                WidgetSnapshotRepo.generate()
            }
            ArkTsBridge.call("syncWidgetSnapshot", snapshot.encode())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // 小组件同步失败不应阻断主流程。
        }
    }

    private fun stopRuntime() {
        runtimeScope?.cancel()
        runtimeScope = null
        resetHome()
        loginModel?.viewModelScope?.cancel()
        loginModel = null
        invalidateNativeFeedback()
        NativeQueries.dispose()
    }

    fun dispose() {
        running = false
        generation++
        stopRuntime()
        scope.cancel()
        requests.clear()
    }

    private fun errorResponse(id: String, message: String): JsonObject = buildJsonObject {
        put("id", id)
        put("data", JsonNull)
        put("error", message)
    }
}
