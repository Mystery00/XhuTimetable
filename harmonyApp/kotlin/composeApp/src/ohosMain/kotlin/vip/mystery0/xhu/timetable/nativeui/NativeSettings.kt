package vip.mystery0.xhu.timetable.nativeui

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.write
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.serialization.json.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.externalPictureDir
import vip.mystery0.xhu.timetable.config.store.*
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.TermStartChangeAlert
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_FILE
import vip.mystery0.xhu.timetable.module.PRIVACY_URL
import vip.mystery0.xhu.timetable.platform.ArkTsBridge
import vip.mystery0.xhu.timetable.repository.BackgroundRepo
import vip.mystery0.xhu.timetable.repository.SchoolCalendarRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.ui.theme.Theme
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.sha256
import kotlin.time.Clock

object NativeSettings : KoinComponent {
    private val fileClient: HttpClient by inject(named(HTTP_CLIENT_FILE))

    suspend fun snapshot(kind: String): JsonObject {
        val firstYear = if (kind == "classSettings") UserStore.loggedUserList().minOfOrNull { it.info.xhuGrade } ?: 2019 else 0
        val items = getConfigStore {
            when (kind) {
                "customUi" -> listOf(
                    setting("todayBackgroundAlpha", "今日卡片透明度", customUi.todayBackgroundAlpha.toString(), "number", "30–100%", "今日课表"),
                    setting("weekItemHeight", "课表格子高度", customUi.weekItemHeight.toString(), "number", "50–150", "本周课表"),
                    setting("weekBackgroundAlpha", "课表格子透明度", customUi.weekBackgroundAlpha.toString(), "number", "30–100%", "本周课表"),
                    setting("weekItemCorner", "课表格子圆角", customUi.weekItemCorner.toString(), "number", "0–24", "本周课表"),
                    setting("weekTitleTextSize", "课程标题字号", customUi.weekTitleTextSize.toString(), "number", "8–18", "本周课表"),
                    setting("backgroundImageBlur", "背景模糊程度", customUi.backgroundImageBlur.toString(), "number", "0–30", "背景"),
                    setting("weekTitleTemplate", "本周课程标题模板", customUi.weekTitleTemplate, "text", "{courseName}、{teacherName}、{location}", "标题模板"),
                    setting("weekNotTitleTemplate", "非本周课程标题模板", customUi.weekNotTitleTemplate, "text", "{courseName}、{teacherName}、{location}", "标题模板"),
                )
                "classSettings" -> {
                    val year = LocalDate.now().year
                    val yearOptions = listOf(option("auto", "自动获取")) + (year downTo firstYear).flatMap { y ->
                        listOf(option("$y:2", "$y-${y + 1}学年 第2学期"), option("$y:1", "$y-${y + 1}学年 第1学期"))
                    }
                    val dateSummary = if (customTermStartDate.custom) {
                        val diff = termStartDate.toEpochDays() - serverTermStartDate.toEpochDays()
                        val difference = when { diff == 0L -> "与云端一致"; diff > 0 -> "比云端推迟 ${TermStartChangeAlert.formatDaysDiff(diff, false)}"; else -> "比云端提前 ${TermStartChangeAlert.formatDaysDiff(-diff, false)}" }
                        "当前设置：${termStartDate.format(dateFormatter)}（已自定义）\n云端最新：${serverTermStartDate.format(dateFormatter)}（$difference）"
                    } else "${termStartDate.format(dateFormatter)}（从云端自动获取）"
                    listOf(
                        setting("showNotThisWeek", "显示非本周课程", showNotThisWeek.toString(), "toggle", "在周课表中以灰阶展示非当前周次的课程", "显示设置", icon = "ic_show_not_this_week"),
                        setting("showStatus", "显示今日课程状态", showStatus.toString(), "toggle", "标注课程的未开始、开课中、已结束状态", "显示设置", icon = "ic_action_show_status"),
                        setting("showTomorrowCourseTime", "自动显示明日课程", showTomorrowCourseTime?.toString().orEmpty(), "time", showTomorrowCourseTime?.let { "每天 ${it.format(Formatter.TIME_NO_SECONDS)} 后自动切换显示明日课表" } ?: "已关闭，点击开关可开启并设定切换时间", "显示设置", icon = "ic_action_switch"),
                        setting("yearTerm", "更改当前学期", if (customNowYear.custom || customNowTerm.custom) "$nowYear:$nowTerm" else "auto", "select", "${nowYear}学年 第${nowTerm}学期 ${if (customNowYear.custom || customNowTerm.custom) "（已自定义）" else "（自动计算）"}", "学期与时间", yearOptions, icon = "ic_action_custom_year_term"),
                        setting("termStartDate", "更改开学时间", termStartDate.toString(), "date", dateSummary, "学期与时间", icon = "ic_action_custom_start_time"),
                        setting("courseColors", "自定义课程颜色", "", "navigate", "为单门课程指定专属主题色", "个性化与数据管理", icon = "ic_action_custom_course_color", route = "courseColors"),
                        setting("showCustomCourseOnWeek", "自定义课程", showCustomCourseOnWeek.toString(), "switchNavigation", if (showCustomCourseOnWeek) "已开启在课表中展示 · 点击管理" else "已隐藏 · 点击管理", "个性化与数据管理", icon = "ic_action_custom_course", route = "customCourses"),
                        setting("showCustomThing", "自定义事项", showCustomThing.toString(), "switchNavigation", if (showCustomThing) "已开启在今日页展示 · 点击管理" else "已隐藏 · 点击管理", "个性化与数据管理", icon = "ic_action_custom_thing", route = "customThings"),
                        setting("schoolCalendar", "查看校历", "", "navigate", "", "实用工具", icon = "ic_action_school_calendar", route = "schoolCalendar"),
                        setting("exportCalendar", "导出到系统日历", "", "action", "鸿蒙系统日历导出尚未接入", "实用工具", icon = "ic_action_export_calendar", enabled = false),
                        setting("showHoliday", "显示节假日信息", showHoliday.toString(), "toggle", "在今日课程顶部提示法定节假日与调休安排", "实用工具", icon = "ic_action_holiday"),
                    )
                }
                else -> listOf(
                    setting("nightMode", "深色与主题模式", nightMode.name, "select", "当前：${nightMode.title}", "外观与界面", listOf(option("AUTO", "自动"), option("ON", "始终开启"), option("OFF", "始终关闭")), icon = "ic_action_night_mode"),
                    setting("customUi", "自定义课表界面", "", "navigate", "调整格子高度、圆角、字号、透明度与模板", "外观与界面", icon = "ic_action_custom_ui", route = "customUi"),
                    setting("backgrounds", "自定义背景图片", "", "navigate", "选择内置壁纸或从相册选择", "外观与界面", icon = "ic_action_background", route = "backgrounds"),
                    setting("disableBackgroundWhenNight", "夜间模式自动禁用背景图", disableBackgroundWhenNight.toString(), "toggle", "开启深色模式时自动使用纯色暗底，保护视力", "外观与界面", icon = "ic_action_disable_when_night"),
                    setting("enableCalendarView", "启用月历日程视图", enableCalendarView.toString(), "toggle", if (multiAccountMode) "多账号模式下暂不支持日历视图" else "以日程流形式浏览整学期课程与事项", "外观与界面", icon = "ic_enable_calendar_view", enabled = !multiAccountMode),
                    setting("resetBackground", "重置启动图隐藏与背景缓存", "", "action", "恢复启动图展示，或清除异常背景图", "外观与界面", icon = "ic_action_clear_splash"),
                    setting("notifyCourse", "课程提醒", "false", "toggle", "鸿蒙系统提醒尚未接入", "通知提醒", icon = "ic_action_notify_course", enabled = false),
                    setting("notifyExam", "考试提醒", "false", "toggle", "鸿蒙系统提醒尚未接入", "通知提醒", icon = "ic_action_notify_exam", enabled = false),
                    setting("notifyTime", "每日提醒时间", "", "time", "鸿蒙系统提醒尚未接入", "通知提醒", icon = "ic_action_notify_time", enabled = false),
                    setting("disablePoems", "禁用今日诗词", PoemsStore.disablePoems.toString(), "toggle", "关闭今日课程顶部的每日诗词卡片", "今日诗词", icon = "ic_poems"),
                    setting("showPoemsTranslate", "显示诗词大意", PoemsStore.showPoemsTranslate.toString(), "toggle", "展开诗词详情时附带译文解析", "今日诗词", icon = "ic_action_view"),
                    setting("resetPoemsToken", "重置诗词 Token", "", "action", "若长期无法显示今日诗词，点击重置缓存凭证", "今日诗词", icon = "ic_sync"),
                    setting("version", "版本与更新日志", "", "navigate", "当前版本：${appVersionName()} (${appVersionCode()})", "关于与支持", icon = "ic_action_check_update", route = "https://blog.mystery0.vip/docs/xgkb/changelog"),
                    setting("website", "西瓜课表官网", "", "navigate", "https://xgkb.mystery0.vip", "关于与支持", icon = "ic_server_detect", route = "https://xgkb.mystery0.vip"),
                    setting("repository", "开源仓库", "", "navigate", "https://github.com/Mystery00/XhuTimetable", "关于与支持", icon = "ic_github", route = "https://github.com/Mystery00/XhuTimetable"),
                    setting("poemsWebsite", "致谢 · 今日诗词", "", "navigate", "感谢作者 @xenv，点击访问官网", "关于与支持", icon = "ic_poems", route = "https://www.jinrishici.com"),
                    setting("privacy", "隐私政策", "", "navigate", "", "关于与支持", icon = "ic_action_view", route = PRIVACY_URL),
                    setting("filing", "ICP 备案号", "", "navigate", "蜀ICP备19031621号-2A", "关于与支持", icon = "ic_admin_status", route = "https://beian.miit.gov.cn"),
                    setting("licenses", "开源依赖项说明", "", "navigate", "", "关于与支持", icon = "ic_notice", route = "about"),
                    setting("pushId", "推送注册 ID", "", "action", "暂无", "关于与支持", icon = "ic_action_notify_course", enabled = false),
                    setting("diagnostics", "上报诊断日志", "", "action", "当前平台尚未启用文件诊断日志", "关于与支持", icon = "ic_feedback", enabled = false),
                ) + if (debugMode) listOf(
                    setting("debugMode", "启用开发者模式", "true", "toggle", "", "开发者选项"),
                    setting("revokePrivacy", "撤销隐私授权", "", "action", "", "开发者选项"),
                ) else emptyList()
            }
        }.toMutableList()
        if (kind == "classSettings") {
            val campus = try {
                val user = UserStore.mainUser()
                val values = user.withAutoLoginOnce { UserRepo.getCampusList(it) }
                setting("campus", "当前主用户校区", values.selected, "select", "当前：${values.selected}", "学期与时间", values.items.map { option(it, it) }, icon = "ic_user_campus")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                setting("campus", "当前主用户校区", "", "select", "当前校区暂未获取，请联网后重试", "学期与时间", icon = "ic_user_campus", enabled = false)
            }
            items.add(5, campus)
        }
        return buildJsonObject { put("items", JsonArray(items)) }
    }

    suspend fun set(key: String, value: String) {
        when (key) {
            "backgroundImage" -> { setBackground(value); return }
            "campus" -> {
                require(value.isNotBlank()) { "请选择校区" }
                UserStore.mainUser().withAutoLoginOnce { UserRepo.updateUserCampus(it, value) }
                return
            }
            "revokePrivacy" -> {
                check(getConfigStore { debugMode }) { "请先启用开发者模式" }
                setCacheStore { allowPrivacy = false }
                return
            }
            "resetBackground" -> {
                setCacheStore { hideSplashBefore = LocalDate.MIN }
                setConfigStore { backgroundImage = null }
                return
            }
            "disablePoems", "showPoemsTranslate", "resetPoemsToken" -> {
                withContext(Dispatchers.IO) {
                    when (key) {
                        "disablePoems" -> PoemsStore.disablePoems = value.toBooleanStrict()
                        "showPoemsTranslate" -> PoemsStore.showPoemsTranslate = value.toBooleanStrict()
                        else -> PoemsStore.token = null
                    }
                }
                return
            }
        }
        setConfigStore {
            fun boolean(): Boolean = value.toBooleanStrictOrNull() ?: error("开关值无效")
            fun decimal(min: Float, max: Float): Float = boundedDecimal(value, min, max)
            when (key) {
                "nightMode" -> nightMode = when (value) { "AUTO" -> NightMode.AUTO; "ON" -> NightMode.ON; "OFF" -> NightMode.OFF; else -> error("主题选项无效") }
                "enableCalendarView" -> { check(!multiAccountMode) { "多账号模式下暂不支持日历视图" }; enableCalendarView = boolean() }
                "debugMode" -> debugMode = boolean()
                "disableBackgroundWhenNight" -> disableBackgroundWhenNight = boolean()
                "showHoliday" -> showHoliday = boolean()
                "showOldCourseWhenFailed" -> showOldCourseWhenFailed = boolean()
                "multiAccountMode" -> multiAccountMode = boolean()
                "showNotThisWeek" -> showNotThisWeek = boolean()
                "showStatus" -> showStatus = boolean()
                "showCustomCourseOnWeek" -> showCustomCourseOnWeek = boolean()
                "showCustomThing" -> showCustomThing = boolean()
                "showTomorrowCourseTime" -> showTomorrowCourseTime = if (value.isBlank()) null else LocalTime.parse(value)
                "yearTerm" -> {
                    if (value == "auto") {
                        customNowYear = Customisable.clearCustom(-1)
                        customNowTerm = Customisable.clearCustom(-1)
                    } else {
                        val parts = value.split(":")
                        val year = parts.getOrNull(0)?.toIntOrNull() ?: error("学年格式无效")
                        val term = parts.getOrNull(1)?.toIntOrNull() ?: error("学期格式无效")
                        require(parts.size == 2 && year in 1900..2200 && term in 1..2) { "学年学期无效" }
                        customNowYear = Customisable.custom(year)
                        customNowTerm = Customisable.custom(term)
                    }
                }
                "termStartDate" -> { customTermStartDate = Customisable.custom(LocalDate.parse(value)); StartRepo.pendingTermStartChangeAlert = null }
                "resetTermStartDate" -> { customTermStartDate = Customisable.clearCustom(LocalDate.MIN); StartRepo.pendingTermStartChangeAlert = null }
                "todayTemplate" -> customAccountTitle = customAccountTitle.copy(todayTemplate = value)
                "weekTemplate" -> customAccountTitle = customAccountTitle.copy(weekTemplate = value)
                "practicalCourseTemplate" -> customAccountTitle = customAccountTitle.copy(practicalCourseTemplate = value)
                "weekItemHeight" -> customUi = customUi.copy(weekItemHeight = decimal(50F, 150F))
                "weekBackgroundAlpha" -> customUi = customUi.copy(weekBackgroundAlpha = decimal(0.3F, 1F))
                "todayBackgroundAlpha" -> customUi = customUi.copy(todayBackgroundAlpha = decimal(0.3F, 1F))
                "weekItemCorner" -> customUi = customUi.copy(weekItemCorner = decimal(0F, 24F))
                "weekTitleTextSize" -> customUi = customUi.copy(weekTitleTextSize = decimal(8F, 18F))
                "backgroundImageBlur" -> customUi = customUi.copy(backgroundImageBlur = decimal(0F, 30F))
                "weekTitleTemplate" -> customUi = customUi.copy(weekTitleTemplate = value.ifBlank { CustomUi.DEFAULT.weekTitleTemplate })
                "weekNotTitleTemplate" -> customUi = customUi.copy(weekNotTitleTemplate = value.ifBlank { CustomUi.DEFAULT.weekNotTitleTemplate })
                "resetUi" -> customUi = CustomUi.DEFAULT
                else -> error("设置项无效")
            }
        }
        if (key == "nightMode") {
            Theme.nightMode.value = getConfigStore { nightMode }
            ArkTsBridge.call("setColorMode", getConfigStore { nightMode.name })
        }
    }

    suspend fun saveAppearance(values: JsonObject) {
        val previous = getConfigStore { customUi }
        fun number(key: String, fallback: Float, min: Float, max: Float) = boundedDecimal(values.text(key, fallback.toString()), min, max)
        val updated = previous.copy(
            todayBackgroundAlpha = number("todayBackgroundAlpha", previous.todayBackgroundAlpha, 0.3F, 1F),
            weekItemHeight = number("weekItemHeight", previous.weekItemHeight, 50F, 150F),
            weekBackgroundAlpha = number("weekBackgroundAlpha", previous.weekBackgroundAlpha, 0.3F, 1F),
            weekItemCorner = number("weekItemCorner", previous.weekItemCorner, 0F, 24F),
            weekTitleTextSize = number("weekTitleTextSize", previous.weekTitleTextSize, 8F, 18F),
            backgroundImageBlur = number("backgroundImageBlur", previous.backgroundImageBlur, 0F, 30F),
            weekTitleTemplate = values.text("weekTitleTemplate", previous.weekTitleTemplate).ifBlank { CustomUi.DEFAULT.weekTitleTemplate },
            weekNotTitleTemplate = values.text("weekNotTitleTemplate", previous.weekNotTitleTemplate).ifBlank { CustomUi.DEFAULT.weekNotTitleTemplate },
        )
        setConfigStore { customUi = updated }
    }

    suspend fun shareSchoolCalendar(url: String) {
        require(SchoolCalendarRepo.getList().any { it.imageUrl == url }) { "校历图片已更新，请刷新后重试" }
        val directory = PlatformFile(externalPictureDir, "school-calendar/${url.sha256()}")
        directory.createDirectories(true)
        val file = PlatformFile(directory, "校历.${imageExtension(url)}")
        if (!file.exists()) file.write(downloadImage(url))
        ArkTsBridge.call("shareFiles", nativeJson.encodeToString(listOf(file.absolutePath())))
    }

    private suspend fun setBackground(url: String) {
        if (url.isBlank() || url == "default") { setConfigStore { backgroundImage = null }; return }
        val selected = BackgroundRepo.getList().firstOrNull { it.imageUrl == url } ?: error("背景图片不存在，请刷新后重试")
        val bytes = downloadImage(selected.imageUrl)
        val directory = PlatformFile(externalPictureDir, "background")
        directory.createDirectories(true)
        val file = PlatformFile(directory, "${url.sha256()}-${Clock.System.now().toEpochMilliseconds()}.${imageExtension(url)}")
        file.write(bytes)
        setConfigStore { backgroundImage = file }
    }

    private suspend fun downloadImage(url: String): ByteArray {
        val response = fileClient.get(url)
        check(response.status.value in 200..299) { "图片下载失败，请稍后重试" }
        return response.body<ByteArray>().also { require(it.isNotEmpty() && it.size <= 25 * 1024 * 1024) { "图片为空或超过25MB" } }
    }

    private fun imageExtension(url: String): String = url.substringBefore('?').substringAfterLast('.', "jpg")
        .lowercase().takeIf { it in setOf("jpg", "jpeg", "png", "webp") } ?: "jpg"

    private fun boundedDecimal(value: String, min: Float, max: Float): Float {
        val number = value.toFloatOrNull() ?: error("请输入有效数字")
        require(number.isFinite() && number in min..max) { "请输入 $min 至 $max 之间的数值" }
        return number
    }

    private fun setting(key: String, title: String, value: String, type: String, summary: String,
        group: String, options: List<JsonObject> = emptyList(), icon: String = "", route: String = "", enabled: Boolean = true): JsonObject = buildJsonObject {
        put("key", key); put("title", title); put("value", value); put("type", type)
        put("summary", summary); put("group", group); put("options", JsonArray(options))
        put("icon", icon); put("route", route); put("enabled", enabled)
    }
}
