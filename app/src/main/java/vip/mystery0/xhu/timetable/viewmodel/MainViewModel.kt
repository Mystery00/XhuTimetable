package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.config.store.MenuStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.model.transfer.AggregationView
import vip.mystery0.xhu.timetable.module.Feature
import vip.mystery0.xhu.timetable.module.betweenDays
import vip.mystery0.xhu.timetable.module.getRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.AggregationRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import vip.mystery0.xhu.timetable.repository.getRawCourseColorList
import vip.mystery0.xhu.timetable.trackError
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.TreeSet

class MainViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private var lastCheckUnreadTime = Instant.MIN

    private val poemsApi: PoemsApi by inject()

    private val feedbackApi: FeedbackApi by inject()

    private val courseRepo: CourseRepo = getRepo()

    private val courseLocalRepo: CourseRepo by localRepo()

    private val customThingRepo: CustomThingRepo = getRepo()

    private val customThingLocalRepo: CustomThingRepo by localRepo()

    private val noticeRepo: NoticeRepo = getRepo()

    private val workManager: WorkManager by inject()

    val version = MutableStateFlow(DataHolder.version)

    private val isDarkMode = MutableStateFlow(false)

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    //今日页面标题
    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    //日期栏开始时间
    private val _dateStart = MutableStateFlow(LocalDate.now())
    val dateStart: StateFlow<LocalDate> = _dateStart

    //当前周
    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    //今日诗词
    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    //今日事项列表
    private val _todayThing = MutableStateFlow<List<TodayThingSheet>>(emptyList())
    val todayThing: StateFlow<List<TodayThingSheet>> = _todayThing

    private val _todayCourse = MutableStateFlow<List<TodayCourseSheet>>(emptyList())
    val todayCourse: StateFlow<List<TodayCourseSheet>> = _todayCourse

    private val _tableCourse = MutableStateFlow<List<List<CourseSheet>>>(emptyList())
    val tableCourse: StateFlow<List<List<CourseSheet>>> = _tableCourse

    private val _weekView = MutableStateFlow<List<WeekView>>(emptyList())
    val weekView: StateFlow<List<WeekView>> = _weekView

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _showWeekView = MutableStateFlow(false)
    val showWeekView: StateFlow<Boolean> = _showWeekView

    private val _hasUnReadNotice = MutableStateFlow(false)
    val hasUnReadNotice: StateFlow<Boolean> = _hasUnReadNotice

    private val _hasUnReadFeedback = MutableStateFlow(false)
    val hasUnReadFeedback: StateFlow<Boolean> = _hasUnReadFeedback

    private val _multiAccountMode = MutableStateFlow(false)
    val multiAccountMode: StateFlow<Boolean> = _multiAccountMode

    private val _showStatus = MutableStateFlow(false)
    val showStatus: StateFlow<Boolean> = _showStatus

    private val _showTomorrowCourse = MutableStateFlow(false)
    val showTomorrowCourse: StateFlow<Boolean> = _showTomorrowCourse

    private val _backgroundImage = MutableStateFlow<Any>(Unit)
    val backgroundImage: StateFlow<Any> = _backgroundImage

    private val _backgroundImageBlur = MutableStateFlow(1)
    val backgroundImageBlur: StateFlow<Int> = _backgroundImageBlur

    private val _emptyUser = MutableStateFlow(false)
    val emptyUser: StateFlow<Boolean> = _emptyUser

    private val _mainUser = MutableStateFlow<User?>(null)
    val mainUser: StateFlow<User?> = _mainUser

    private val _menu = MutableStateFlow<List<List<Menu>>>(emptyList())
    val menu: StateFlow<List<List<Menu>>> = _menu

    private val _customUi = MutableStateFlow(CustomUi.DEFAULT)
    val customUi: StateFlow<CustomUi> = _customUi

    init {
        viewModelScope.launch {
            loadFromConfig()
            val mainUser = UserStore.getMainUser()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
        showPoems()
        calculateTodayTitle()
        calculateWeek()

        loadLocalDataToState()
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    private suspend fun loadFromConfig() {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        _showStatus.value = getConfigStore { showStatus }
        _showTomorrowCourse.value = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        _menu.value = MenuStore.loadAllMenu()
        _customUi.value = getConfigStore { customUi }
    }

    fun loadConfig() {
        viewModelScope.launch {
            loadFromConfig()
        }
    }

    fun loadBackground(isDarkModeValue: Boolean = isDarkMode.value) {
        viewModelScope.launch {
            isDarkMode.value = isDarkModeValue
            val disable = getConfig { disableBackgroundWhenNight }
            val isNight = isDarkMode.value
            if (disable && isNight) {
                _backgroundImage.value = Unit
                return@launch
            } else {
                _backgroundImage.value =
                    getConfig { backgroundImage } ?: XhuImages.defaultBackgroundImage
            }
            _backgroundImageBlur.value = getConfig { customUi }.backgroundImageBlur
        }
    }

    fun clearLastCheckUnreadTime() {
        lastCheckUnreadTime = Instant.MIN
    }

    fun checkUnRead(block: () -> Unit) {
        val now = Instant.now()
        if (Duration.between(lastCheckUnreadTime, now) > Duration.ofMinutes(1)) {
            block()
            lastCheckUnreadTime = now
        }
    }

    fun checkMainUser() {
        viewModelScope.launch {
            val mainUser = UserStore.getMainUser()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
    }

    private fun showPoems() {
        if (!isOnline()) {
            return
        }
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "showPoems: ", throwable)
            trackError(throwable)
        }) {
            if (PoemsStore.disablePoems) {
                return@launch
            }
            if (!Feature.JRSC.isEnabled()) {
                return@launch
            }
            val token = PoemsStore.token
            if (token.isNullOrBlank()) {
                PoemsStore.token = poemsApi.getToken().data
            }
            val poems = poemsApi.getSentence().data
            if (!PoemsStore.showPoemsTranslate) {
                poems.origin.translate = null
            }
            _poems.value = poems
        }
    }

    fun calculateTodayTitle() {
        viewModelScope.launch {
            val showTomorrow = getConfigStore { showTomorrowCourseTime }?.let {
                LocalTime.now().isAfter(it)
            } ?: false

            val termStartDate = getConfigStore { termStartDate }
            val now = LocalDate.now()
            val todayWeekIndex =
                now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            if (now.isBefore(termStartDate)) {
                //开学之前，那么计算剩余时间
                val remainDays = betweenDays(now, termStartDate)
                _todayTitle.value = "距离开学还有${remainDays}天 $todayWeekIndex"
                return@launch
            }
            val date = if (showTomorrow) now.plusDays(1) else now
            val days = betweenDays(termStartDate, date)
            val weekIndex = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            val week = (days / 7) + 1
            if (week > 20) {
                _todayTitle.value = "本学期已结束"
            } else {
                _todayTitle.value = "第${week}周 $weekIndex"
            }
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            _week.value = calculateWeekInternal()
            //切换周时同步切换课表
            _week.collect {
                val termStartDate = getConfigStore { termStartDate }
                val currentWeek = calculateWeekInternal()
                //获取缓存的课程数据
                val data = getMainPageData(false)
                val weekList = data.weekViewList
                loadCourseToTable(currentWeek, it, weekList, true)
                _dateStart.value = termStartDate.plusWeeks(it.toLong() - 1)
                    //设置一周开始为周一
                    .with(WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek(), 1)
            }
        }
    }

    /**
     * 计算当前周数，如果未开学，该方法返回0
     */
    private suspend fun calculateWeekInternal(date: LocalDate = LocalDate.now()): Int {
        val termStartDate = getConfigStore { termStartDate }
        val days = betweenDays(termStartDate, date)
        var week = ((days / 7) + 1)
        if (days < 0 && week > 0) {
            week = 0
        }
        return week
    }

    private suspend fun getMainPageData(forceLoadFromCloud: Boolean): AggregationView {
        val showCustomCourse = getConfigStore { showCustomCourseOnWeek }
        val showCustomThing = getConfigStore { showCustomThing }
        return AggregationRepo.fetchAggregationMainPage(
            forceLoadFromCloud,
            showCustomCourse,
            showCustomThing,
        )
    }

    /**
     * 初始化时候加载数据的方法
     */
    fun loadLocalDataToState() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load local course list failed", throwable)
            _loading.value = false
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //设置加载状态
            _loading.value = true
            //加载课表相关配置项
            val pair = loadCourseConfig(forceUpdate = false)
            val currentWeek = pair.first
            val loadFromCloud = pair.second
            //获取缓存的课程数据
            val data = getMainPageData(false)
            //加载今日列表的数据
            loadTodayCourse(currentWeek, data.todayViewList)
            //加载今日事项
            loadTodayThing(data.todayThingList)
            //加载周列表的数据
            loadCourseToTable(currentWeek, currentWeek, data.weekViewList, false)

            if (loadFromCloud) {
                //需要从云端加载数据
                val cloudData = getMainPageData(true)
                //加载今日列表的数据
                loadTodayCourse(currentWeek, cloudData.todayViewList)
                //加载今日事项
                loadTodayThing(cloudData.todayThingList)
                //加载周列表的数据
                loadCourseToTable(currentWeek, currentWeek, cloudData.weekViewList, false)
                toastMessage("数据同步成功！")
            }
            _loading.value = false
        }
    }

    /**
     * 手动刷新加载数据的方法
     */
    fun refreshCloudDataToState() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load course list failed", throwable)
            _loading.value = false
            if (!GlobalConfigStore.showOldCourseWhenFailed) {
                _tableCourse.value = emptyList()
            }
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //设置加载状态
            _loading.value = true
            //加载课表相关配置项
            val pair = loadCourseConfig(forceUpdate = false)
            val currentWeek = pair.first
            //从云端加载数据
            val cloudData = getMainPageData(true)
            //加载今日列表的数据
            loadTodayCourse(currentWeek, cloudData.todayViewList)
            //加载今日事项
            loadTodayThing(cloudData.todayThingList)
            //加载周列表的数据
            loadCourseToTable(currentWeek, currentWeek, cloudData.weekViewList, false)
            toastMessage("数据同步成功！")
            _loading.value = false
        }
    }

    /**
     * 复用的加载课表相关配置项的方法
     * @return 是否需要从云端加载课表
     */
    private suspend fun loadCourseConfig(forceUpdate: Boolean = true): Pair<Int, Boolean> {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        var loadFromCloud = forceUpdate
        if (!loadFromCloud) {
            loadFromCloud = getConfigStore { lastSyncCourse }.isBefore(LocalDate.now())
        }
        val currentWeek = calculateWeekInternal()
        _week.value = currentWeek

        return currentWeek to loadFromCloud
    }

    /**
     * 复用的加载今日课表的方法
     * @param currentWeek 当前周
     * @param courseList 课程列表
     */
    private suspend fun loadTodayCourse(
        currentWeek: Int,
        courseList: List<TodayCourseView>,
    ) {
        //获取自定义颜色列表
        val colorMap = getRawCourseColorList()
        val today = LocalDate.now()
        val showTomorrow = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        val showDate: LocalDate
        val showDay: DayOfWeek
        val showCurrentWeek: Int
        if (showTomorrow) {
            //显示明天的课程，那么showDate就是明天对应的日期
            showDate = today.plusDays(1)
            showDay = showDate.dayOfWeek
            showCurrentWeek = calculateWeekInternal(showDate)
        } else {
            showDate = today
            showDay = today.dayOfWeek
            showCurrentWeek = currentWeek
        }
        //过滤出今日或者明日的课程
        val showList =
            courseList.filter { it.weekList.contains(showCurrentWeek) && it.day == showDay }
                .sortedBy { it.startDayTime }
        if (showList.isEmpty()) {
            _todayCourse.value = emptyList()
            return
        }
        //合并相同课程
        val resultList = ArrayList<TodayCourseView>(showList.size)
        //计算key与设置颜色
        showList.forEach {
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        showList.groupBy { it.user.studentId }
            .forEach { (_, list) ->
                var last = list.first()
                var lastKey = last.key
                list.forEachIndexed { index, todayCourseView ->
                    if (index == 0) {
                        resultList.add(todayCourseView)
                        return@forEachIndexed
                    }
                    val thisKey = todayCourseView.key
                    if (lastKey == thisKey) {
                        if (last.endDayTime == todayCourseView.startDayTime - 1) {
                            //合并两个课程
                            last.endDayTime = todayCourseView.endDayTime
                        } else {
                            //两个课程相同但是节次不连续，不合并
                            resultList.add(todayCourseView)
                            last = todayCourseView
                            lastKey = thisKey
                        }
                    } else {
                        resultList.add(todayCourseView)
                        last = todayCourseView
                        lastKey = thisKey
                    }
                }
                if (resultList.last() != last) {
                    resultList.add(last)
                }
            }
        //最后按照开始节次排序
        val now = LocalDateTime.now()
        _todayCourse.value = resultList.sortedBy { it.startDayTime }
            .map {
                it.updateTime()
                TodayCourseSheet(
                    it.courseName,
                    it.teacher,
                    TreeSet<Int>().apply {
                        addAll(it.startDayTime..it.endDayTime)
                    },
                    it.courseTime,
                    it.courseDayTime,
                    it.location,
                    it.user.studentId,
                    it.user.info.name,
                    it.backgroundColor,
                    showDate,
                ).calc(now)
            }
    }

    /**
     * 复用的加载今日事项的方法
     * @param thingList 事项列表
     */
    private suspend fun loadTodayThing(thingList: List<TodayThingView>) {
        val showTomorrow = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        val showInstant: Instant
        if (showTomorrow) {
            //显示明天的事项，那么showInstant就是明天开始的时间
            showInstant = LocalDate.now().plusDays(1).atStartOfDay().asInstant()
        } else {
            showInstant = Instant.now()
        }
        //过滤出今日或者明日的课程
        val showList = thingList
            .filter { it.showOnDay(showInstant) }
            .sortedBy { it.sort }
        if (showList.isEmpty()) {
            _todayThing.value = emptyList()
            return
        }
        _todayThing.value = showList.map {
            val startDateTime = it.startTime.asLocalDateTime()
            val endDateTime = it.endTime.asLocalDateTime()
            val timeText = buildString {
                if (it.allDay) {
                    append(startDateTime.format(dateFormatter))
                } else {
                    append(startDateTime.format(thingDateTimeFormatter))
                }
                if (it.saveAsCountDown) {
                    return@buildString
                }
                append(" - ")
                if (it.allDay) {
                    append(endDateTime.format(dateFormatter))
                } else {
                    append(endDateTime.format(thingDateTimeFormatter))
                }
            }
            val remainDays =
                Duration.between(LocalDate.now().atStartOfDay(), startDateTime).toDays()
            TodayThingSheet(
                it.title,
                it.location,
                it.allDay,
                timeText,
                it.remark,
                it.color,
                it.saveAsCountDown,
                remainDays,
                it.user.studentId,
                it.user.info.name,
            )
        }
    }

    /**
     * 复用的加载本周课表的方法
     * @param currentWeek 当前周
     * @param showWeek 需要显示周
     * @param courseList 课程列表
     * @param changeWeekOnly 是否只是切换周数
     */
    private suspend fun loadCourseToTable(
        currentWeek: Int,
        showWeek: Int,
        courseList: List<WeekCourseView>,
        changeWeekOnly: Boolean = false,
    ) {
        //获取自定义颜色列表
        val colorMap = getRawCourseColorList()
        //设置是否本周以及课程颜色
        courseList.forEach {
            it.thisWeek = it.weekList.contains(showWeek)
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        //组建表格的数据结构
        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                CourseSheet.empty(1, index + 1, day + 1)
            }
        }
        //平铺课程
        courseList.forEach { course ->
            val day = course.day.value - 1
            (course.startDayTime..course.endDayTime).forEach { time ->
                //填充表格
                expandTableCourse[day][time - 1].course.add(course)
            }
        }
        //使用key判断格子内容是否相同，相同则合并
        val tableCourse = Array(7) { day ->
            val dayArray = expandTableCourse[day]
            val first = dayArray.first()
            val result = ArrayList<CourseSheet>(dayArray.size)
            var last = first
            var lastKey = first.course.sortedBy { it.key }.joinToString { it.key }
            dayArray.forEachIndexed { index, courseSheet ->
                if (index == 0) {
                    return@forEachIndexed
                }
                val thisKey = courseSheet.course.sortedBy { it.key }.joinToString { it.key }
                if (lastKey == thisKey) {
                    last.step++
                } else {
                    result.add(last)
                    last = courseSheet
                    lastKey = thisKey
                }
            }
            if (result.last() != last) {
                result.add(last)
            }
            result
        }
        //处理显示的信息
        tableCourse.forEach { array ->
            array.forEach { sheet ->
                if (sheet.course.isNotEmpty()) {
                    sheet.course.sort()
                    val show = sheet.course.first()
                    sheet.course = ArrayList(sheet.course.sortedBy { it.weekList.first() })
                    if (show.thisWeek) {
                        sheet.showTitle = show.format(_customUi.value.weekTitleTemplate)
                        sheet.color = colorMap[show.courseName] ?: ColorPool.hash(show.courseName)
                        sheet.textColor = Color.White
                    } else {
                        sheet.showTitle = show.format(_customUi.value.weekNotTitleTemplate)
                        sheet.color = XhuColor.notThisWeekBackgroundColor
                        sheet.textColor = Color.Gray
                    }
                }
            }
        }
        //过滤非本周课程
        val showNotThisWeek = getConfigStore { showNotThisWeek }
        if (!showNotThisWeek) {
            tableCourse.forEach { array ->
                array.forEach { sheet ->
                    sheet.course.removeIf { !it.thisWeek }
                }
            }
        }
        _tableCourse.value = tableCourse.toList()

        if (changeWeekOnly) {
            //仅仅只是切换周，那么不需要重新渲染周预览
            return
        }

        //计算顶部周视图
        val weekViewArray = Array(20) { index ->
            WeekView(index + 1, currentWeek == index + 1, Array(5) { Array(5) { false } })
        }
        for (dayIndex in 0 until 5) {
            val dayArray = expandTableCourse[dayIndex]
            for (itemIndex in 0 until 10) {
                val sheet = dayArray[itemIndex]
                sheet.course.forEach {
                    it.weekList.forEach { week ->
                        weekViewArray[week - 1].array[dayIndex][itemIndex / 2] = true
                    }
                }
            }
        }
        _weekView.value = weekViewArray.toList()
    }

    fun animeWeekView() {
        _showWeekView.value = !_showWeekView.value
    }

    fun dismissWeekView() {
        _showWeekView.value = false
    }

    fun changeCurrentWeek(currentWeek: Int) {
        _week.value = currentWeek
    }

    fun checkUnReadNotice() {
//        viewModelScope.launch(serverExceptionHandler { throwable ->
//            Log.w(TAG, "load notice list failed", throwable)
//            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
//        }) {
//            if (UserStore.getMainUser() == null) return@launch
//            _hasUnReadNotice.value = noticeRepo.hasUnReadNotice()
//        }
    }

    fun checkUnReadFeedback() {
//        if (!isOnline()) {
//            return
//        }
//        viewModelScope.launch(serverExceptionHandler { throwable ->
//            Log.w(TAG, "check unread feedback failed", throwable)
//        }) {
//            if (UserStore.getMainUser() == null) return@launch
//            val firstFeedbackMessageId = getConfig { firstFeedbackMessageId }
//            val response = UserStore.mainUser().withAutoLogin {
//                feedbackApi.checkMessage(it, firstFeedbackMessageId).checkLogin()
//            }
//            _hasUnReadFeedback.value = response.first.newResult
//        }
    }

    fun downloadApk() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadApkWork>()
                    .build()
            )
        }
    }

    fun downloadPatch() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadPatchWork>()
                    .build()
            )
        }
    }

    fun ignoreVersion(version: Version) {
        viewModelScope.launch {
            val ignore = getConfig { ignoreVersionList }
            ignore.add("${version.versionName}-${version.versionCode}")
            setConfig { ignoreVersionList = ignore }
        }
    }
}

data class CourseSheet(
    //显示标题
    var showTitle: String,
    //步长
    var step: Int,
    //开始节次序号
    val startIndex: Int,
    //周几
    val day: Int,
    //当前格子的课程列表
    var course: ArrayList<WeekCourseView>,
    //颜色
    var color: Color,
    //文本颜色
    var textColor: Color,
) {
    companion object {
        fun empty(step: Int, startIndex: Int, day: Int): CourseSheet = CourseSheet(
            "", step, startIndex, day, arrayListOf(), Color.Unspecified, Color.Unspecified
        )
    }

    fun isEmpty(): Boolean = course.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseSheet

        if (showTitle != other.showTitle) return false
        if (course != other.course) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showTitle.hashCode()
        result = 31 * result + course.hashCode()
        return result
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

data class TodayCourseSheet(
    val courseName: String,
    val teacherName: String,
    val timeSet: TreeSet<Int>,
    val timeText: Pair<String, String>,
    val timeString: String,
    val location: String,
    val studentId: String,
    val userName: String,
    val color: Color,
    val showDate: LocalDate,
) {
    lateinit var courseStatus: CourseStatus

    fun calc(now: LocalDateTime): TodayCourseSheet {
        val startTime = LocalTime.parse(timeText.first, timeFormatter).atDate(showDate)
        val endTime = LocalTime.parse(timeText.second, timeFormatter).atDate(showDate)
        courseStatus = when {
            now.isBefore(startTime) -> CourseStatus.BEFORE
            now.isAfter(endTime) -> CourseStatus.AFTER
            else -> CourseStatus.IN
        }
        return this
    }
}

data class TodayThingSheet(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val timeText: String,
    val remark: String,
    val color: Color,
    val saveAsCountDown: Boolean,
    val remainDays: Long,
    val studentId: String,
    val userName: String,
)

enum class CourseStatus(
    val title: String,
    val color: Color,
    val backgroundColor: Color,
) {
    BEFORE("未开始", XhuColor.Status.beforeColor, XhuColor.Status.beforeBackgroundColor),
    IN("开课中", XhuColor.Status.inColor, XhuColor.Status.inBackgroundColor),
    AFTER("已结束", XhuColor.Status.afterColor, XhuColor.Status.afterBackgroundColor),
}

class WeekView(
    val weekNum: Int,
    val thisWeek: Boolean,
    val array: Array<Array<Boolean>>
)
