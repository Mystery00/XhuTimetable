package vip.mystery0.xhu.timetable.config.store

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.isAbsolute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.model.CustomAccountTitle
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.utils.MIN

object Formatter {
    val ZONE_CHINA: TimeZone = TimeZone.of("Asia/Shanghai")

    val DATE = LocalDate.Formats.ISO
    val TIME = LocalTime.Formats.ISO
    val TIME_NO_SECONDS = LocalTime.Format {
        hour()
        char(':')
        minute()
    }
}

private val instance = ConfigStore()
val GlobalConfigStore = instance

suspend fun <T> getConfigStore(block: ConfigStore.() -> T) =
    withContext(Dispatchers.IO) { block(instance) }

suspend fun setConfigStore(block: suspend ConfigStore.() -> Unit) =
    withContext(Dispatchers.IO) { block(instance) }

class ConfigStore internal constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    // 用户信息存储密钥
    private val userStoreSecretKey = "userStoreSecret"
    var userStoreSecret: String
        set(value) {
            Store.ConfigStore.setConfiguration(userStoreSecretKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(userStoreSecretKey, "")

    //开学时间
    private val termStartDateKey = "termStartDate"
    val termStartDate: LocalDate
        get() = customTermStartDate.data
    var customTermStartDate: Customisable<LocalDate>
        set(value) {
            if (!value.custom && value.data == LocalDate.MIN) {
                //清除自定义值
                Store.ConfigStore.removeConfiguration(Customisable.customKey(termStartDateKey))
                return
            }
            val key = value.mapKey(termStartDateKey)
            val saveValue = value.data.format(Formatter.DATE)
            Store.ConfigStore.setConfiguration(key, saveValue)
        }
        get() {
            val customValue =
                Store.ConfigStore.getConfiguration(Customisable.customKey(termStartDateKey), "")
            if (customValue.isNotBlank()) {
                return Customisable(LocalDate.parse(customValue, Formatter.DATE), true)
            }
            val value = Store.ConfigStore.getConfiguration(termStartDateKey, "")
            if (value.isNotBlank()) {
                return Customisable(LocalDate.parse(value, Formatter.DATE), false)
            }
            // 默认值
            return Customisable(LocalDate(2025, 9, 8), false)
        }

    //当前学年
    private val nowYearKey = "nowYear"
    val nowYear: Int
        get() = customNowYear.data
    var customNowYear: Customisable<Int>
        set(value) {
            if (!value.custom && value.data == -1) {
                //清除自定义值
                Store.ConfigStore.removeConfiguration(Customisable.customKey(nowYearKey))
                return
            }
            val key = value.mapKey(nowYearKey)
            val saveValue = value.data
            Store.ConfigStore.setConfiguration(key, saveValue)
        }
        get() {
            val customValue: Int =
                Store.ConfigStore.getConfiguration(Customisable.customKey(nowYearKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = Store.ConfigStore.getConfiguration(nowYearKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(2025, false)
        }

    //当前学期
    private val nowTermKey = "nowTerm"
    val nowTerm: Int
        get() = customNowTerm.data
    var customNowTerm: Customisable<Int>
        set(value) {
            if (!value.custom && value.data == -1) {
                //清除自定义值
                Store.ConfigStore.removeConfiguration(Customisable.customKey(nowTermKey))
                return
            }
            val key = value.mapKey(nowTermKey)
            val saveValue = value.data
            Store.ConfigStore.setConfiguration(key, saveValue)
        }
        get() {
            val customValue: Int =
                Store.ConfigStore.getConfiguration(Customisable.customKey(nowTermKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = Store.ConfigStore.getConfiguration(nowTermKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(1, false)
        }

    //在周课表中显示自定义课程
    private val showCustomCourseOnWeekKey = "showCustomCourseOnWeek"
    var showCustomCourseOnWeek: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showCustomCourseOnWeekKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showCustomCourseOnWeekKey, true)

    //多用户模式
    private val multiAccountModeKey = "multiAccountMode"
    var multiAccountMode: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(multiAccountModeKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(multiAccountModeKey, false)

    //自定义账号标题模板
    private val customAccountTitleKey = "customAccountTitle"
    var customAccountTitle: CustomAccountTitle
        set(value) {
            Store.ConfigStore.setConfiguration(
                customAccountTitleKey,
                json.encodeToString(value)
            )
        }
        get() {
            val save = Store.ConfigStore.getConfiguration(customAccountTitleKey, "")
            return if (save.isBlank()) CustomAccountTitle.DEFAULT
            else json.decodeFromString(save)
        }

    //显示非本周课程
    private val showNotThisWeekKey = "showNotThisWeek"
    var showNotThisWeek: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showNotThisWeekKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showNotThisWeekKey, true)

    //到达指定时间之后显示明日的课程
    private val showTomorrowCourseTimeKey = "showTomorrowCourseTime"
    var showTomorrowCourseTime: LocalTime?
        set(value) {
            if (value == null) {
                Store.ConfigStore.removeConfiguration(showTomorrowCourseTimeKey)
            } else {
                Store.ConfigStore.setConfiguration(
                    showTomorrowCourseTimeKey,
                    value.format(Formatter.TIME)
                )
            }
        }
        get() {
            val time = Store.ConfigStore.getConfiguration(showTomorrowCourseTimeKey, "")
            if (time.isBlank()) {
                return null
            }
            return LocalTime.parse(time, Formatter.TIME)
        }

    //显示课程状态
    private val showStatusKey = "showStatus"
    var showStatus: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showStatusKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showStatusKey, true)

    //自定义课表格子
    private val customUiKey = "customUi"
    var customUi: CustomUi
        set(value) {
            Store.ConfigStore.setConfiguration(
                customUiKey,
                json.encodeToString(value)
            )
        }
        get() {
            val save = Store.ConfigStore.getConfiguration(customUiKey, "")
            return if (save.isBlank()) CustomUi.DEFAULT
            else json.decodeFromString(save)
        }

    //当加载失败时显示旧数据
    private val showOldCourseWhenFailedKey = "showOldCourseWhenFailed"
    var showOldCourseWhenFailed: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showOldCourseWhenFailedKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showOldCourseWhenFailedKey, true)

    //在今日课程页面显示自定义事项
    private val showCustomThingKey = "showCustomThing"
    var showCustomThing: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showCustomThingKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showCustomThingKey, true)

    //版本更新渠道
    private val versionChannelKey = "versionChannel"
    var versionChannel: VersionChannel
        set(value) {
            Store.ConfigStore.setConfiguration(versionChannelKey, value.value)
        }
        get() {
            val save =
                Store.ConfigStore.getConfiguration(versionChannelKey, VersionChannel.STABLE.value)
            return VersionChannel.parse(save)
        }

    //夜间模式禁用背景图
    private val disableBackgroundWhenNightKey = "disableBackgroundWhenNight"
    var disableBackgroundWhenNight: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(disableBackgroundWhenNightKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(disableBackgroundWhenNightKey, true)

    //启用日历视图
    private val enableCalendarViewKey = "enableCalendarView"
    var enableCalendarView: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(enableCalendarViewKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(enableCalendarViewKey, true)

    //课程提醒开关
    private val notifyCourseKey = "notifyCourse"
    var notifyCourse: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(notifyCourseKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(notifyCourseKey, true)

    //考试提醒开关
    private val notifyExamKey = "notifyExam"
    var notifyExam: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(notifyExamKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(notifyExamKey, true)

    //提醒时间
    private val notifyTimeKey = "notifyTime"
    var notifyTime: LocalTime?
        set(value) {
            if (value == null) {
                Store.ConfigStore.removeConfiguration(notifyTimeKey)
                return
            }
            Store.ConfigStore.setConfiguration(notifyTimeKey, value.format(Formatter.TIME))
        }
        get() {
            val date = Store.ConfigStore.getConfiguration(notifyTimeKey, "")
            if (date.isBlank()) {
                return null
            }
            return LocalTime.parse(date, Formatter.TIME)
        }

    //发送错误报告
    private val allowSendCrashReportKey = "allowSendCrashReport"
    var allowSendCrashReport: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(allowSendCrashReportKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(allowSendCrashReportKey, false)

    //夜间模式
    private val nightModeKey = "nightMode"
    var nightMode: NightMode
        set(value) {
            Store.ConfigStore.setConfiguration(nightModeKey, value.value)
        }
        get() {
            val value = Store.ConfigStore.getConfiguration(nightModeKey, NightMode.AUTO.value)
            return NightMode.entries.first { it.value == value }
        }

    //开发者模式
    private val debugModeKey = "debugMode"
    var debugMode: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(debugModeKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(debugModeKey, false)

    //始终显示崩溃信息
    private val alwaysCrashKey = "alwaysCrash"
    var alwaysCrash: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(alwaysCrashKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(alwaysCrashKey, debugMode)

    //自定义课表背景图
    private val backgroundImageKey = "backgroundImage"
    var backgroundImage: PlatformFile?
        set(value) {
            if (value != null) {
                Store.ConfigStore.setConfiguration(backgroundImageKey, value.absolutePath())
            } else {
                Store.ConfigStore.removeConfiguration(backgroundImageKey)
            }
        }
        get() {
            val image = Store.ConfigStore.getConfiguration(backgroundImageKey, "")
            if (image.isBlank()) return null
            val file = PlatformFile(image)
            if (!file.isAbsolute()) return null
            return file
        }

    //显示节假日信息
    private val showHolidayKey = "showHoliday"
    var showHoliday: Boolean
        set(value) {
            Store.ConfigStore.setConfiguration(showHolidayKey, value)
        }
        get() = Store.ConfigStore.getConfiguration(showHolidayKey, true)
}