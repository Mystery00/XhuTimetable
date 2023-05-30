package vip.mystery0.xhu.timetable.config.store

import com.squareup.moshi.Moshi
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.module.registerAdapter
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Formatter {
    val ZONE_CHINA: ZoneId = ZoneId.of("Asia/Shanghai")

    val DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val TIME: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    val TIME_NO_SECONDS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
}

private val instance = ConfigStore()
val GlobalConfigStore = instance

suspend fun <T> getConfigStore(block: ConfigStore.() -> T) =
    withContext(Dispatchers.IO) { block(instance) }

suspend fun setConfigStore(block: suspend ConfigStore.() -> Unit) =
    withContext(Dispatchers.IO) { block(instance) }

class ConfigStore internal constructor() {
    private val kv = MMKV.mmkvWithID("ConfigStore")
    private val moshi = Moshi.Builder().registerAdapter().build()

    // 用户信息存储密钥
    private val userStoreSecretKey = "userStoreSecret"
    var userStoreSecret: String
        set(value) {
            kv.encode(userStoreSecretKey, value)
        }
        get() = kv.decodeString(userStoreSecretKey) ?: ""

    //开学时间
    private val termStartDateKey = "termStartDate"
    val termStartDate: LocalDate
        get() = customTermStartDate.data
    var customTermStartDate: Customisable<LocalDate>
        set(value) {
            if (!value.custom && value.data == LocalDate.MIN) {
                //清除自定义值
                kv.remove(Customisable.customKey(termStartDateKey))
                return
            }
            val key = value.mapKey(termStartDateKey)
            val saveValue = value.data.format(Formatter.DATE)
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: String? = kv.decodeString(Customisable.customKey(termStartDateKey))
            if (!customValue.isNullOrBlank()) {
                return Customisable(LocalDate.parse(customValue, Formatter.DATE), true)
            }
            val value: String? = kv.decodeString(termStartDateKey)
            if (!value.isNullOrBlank()) {
                return Customisable(LocalDate.parse(value, Formatter.DATE), false)
            }
            // 默认值
            return Customisable(LocalDate.of(2023, 2, 20), false)
        }

    //当前学年
    private val nowYearKey = "nowYear"
    val nowYear: Int
        get() = customNowYear.data
    var customNowYear: Customisable<Int>
        set(value) {
            if (!value.custom && value.data == -1) {
                //清除自定义值
                kv.remove(Customisable.customKey(nowYearKey))
                return
            }
            val key = value.mapKey(nowYearKey)
            val saveValue = value.data
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: Int = kv.decodeInt(Customisable.customKey(nowYearKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = kv.decodeInt(nowYearKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(2022, false)
        }

    //当前学期
    private val nowTermKey = "nowTerm"
    val nowTerm: Int
        get() = customNowTerm.data
    var customNowTerm: Customisable<Int>
        set(value) {
            if (!value.custom && value.data == -1) {
                //清除自定义值
                kv.remove(Customisable.customKey(nowTermKey))
                return
            }
            val key = value.mapKey(nowTermKey)
            val saveValue = value.data
            kv.encode(key, saveValue)
        }
        get() {
            val customValue: Int = kv.decodeInt(Customisable.customKey(nowTermKey), -1)
            if (customValue != -1) {
                return Customisable(customValue, true)
            }
            val value: Int = kv.decodeInt(nowTermKey, -1)
            if (value != -1) {
                return Customisable(value, false)
            }
            // 默认值
            return Customisable(2, false)
        }

    //在周课表中显示自定义课程
    private val showCustomCourseOnWeekKey = "showCustomCourseOnWeek"
    var showCustomCourseOnWeek: Boolean
        set(value) {
            kv.encode(showCustomCourseOnWeekKey, value)
        }
        get() = kv.decodeBool(showCustomCourseOnWeekKey, true)

    //多用户模式
    private val multiAccountModeKey = "multiAccountMode"
    var multiAccountMode: Boolean
        set(value) {
            kv.encode(multiAccountModeKey, value)
        }
        get() = kv.decodeBool(multiAccountModeKey, false)

    //显示非本周课程
    private val showNotThisWeekKey = "showNotThisWeek"
    var showNotThisWeek: Boolean
        set(value) {
            kv.encode(showNotThisWeekKey, value)
        }
        get() = kv.decodeBool(showNotThisWeekKey, true)

    //到达指定时间之后显示明日的课程
    private val showTomorrowCourseTimeKey = "showTomorrowCourseTime"
    var showTomorrowCourseTime: LocalTime?
        set(value) {
            kv.encode(showTomorrowCourseTimeKey, value?.format(Formatter.TIME))
        }
        get() = kv.decodeString(showTomorrowCourseTimeKey)
            ?.let { LocalTime.parse(it, Formatter.TIME) }

    //显示课程状态
    private val showStatusKey = "showStatus"
    var showStatus: Boolean
        set(value) {
            kv.encode(showStatusKey, value)
        }
        get() = kv.decodeBool(showStatusKey, true)

    //自定义课表格子
    private val customUiKey = "customUi"
    var customUi: CustomUi
        set(value) {
            kv.encode(customUiKey, moshi.adapter(CustomUi::class.java).toJson(value))
        }
        get() {
            val save = kv.decodeString(customUiKey, "")
            return if (save.isNullOrBlank()) CustomUi.DEFAULT
            else moshi.adapter(CustomUi::class.java).fromJson(save)!!
        }

    //当加载失败时显示旧数据
    private val showOldCourseWhenFailedKey = "showOldCourseWhenFailed"
    var showOldCourseWhenFailed: Boolean
        set(value) {
            kv.encode(showOldCourseWhenFailedKey, value)
        }
        get() = kv.decodeBool(showOldCourseWhenFailedKey, true)

    //在今日课程页面显示自定义事项
    private val showCustomThingKey = "showCustomThing"
    var showCustomThing: Boolean
        set(value) {
            kv.encode(showCustomThingKey, value)
        }
        get() = kv.decodeBool(showCustomThingKey, true)

    //版本更新渠道
    private val versionChannelKey = "versionChannel"
    var versionChannel: VersionChannel
        set(value) {
            kv.encode(versionChannelKey, value.value)
        }
        get() {
            val save = kv.decodeInt(versionChannelKey, VersionChannel.STABLE.value)
            return VersionChannel.parse(save)
        }

    //夜间模式禁用背景图
    private val disableBackgroundWhenNightKey = "disableBackgroundWhenNight"
    var disableBackgroundWhenNight: Boolean
        set(value) {
            kv.encode(disableBackgroundWhenNightKey, value)
        }
        get() = kv.decodeBool(disableBackgroundWhenNightKey, true)

    //课程提醒开关
    private val notifyCourseKey = "notifyCourse"
    var notifyCourse: Boolean
        set(value) {
            kv.encode(notifyCourseKey, value)
        }
        get() = kv.decodeBool(notifyCourseKey, true)

    //考试提醒开关
    private val notifyExamKey = "notifyExam"
    var notifyExam: Boolean
        set(value) {
            kv.encode(notifyExamKey, value)
        }
        get() = kv.decodeBool(notifyExamKey, true)

    //提醒时间
    private val notifyTimeKey = "notifyTime"
    var notifyTime: LocalTime?
        set(value) {
            if (value == null) {
                kv.remove(notifyTimeKey)
                return
            }
            kv.encode(notifyTimeKey, value.format(Formatter.TIME))
        }
        get() {
            val date = kv.decodeString(notifyTimeKey)
            if (date.isNullOrBlank()) {
                return null
            }
            return LocalTime.parse(date, Formatter.TIME)
        }

    //发送错误报告
    private val allowSendCrashReportKey = "allowSendCrashReport"
    var allowSendCrashReport: Boolean
        set(value) {
            kv.encode(allowSendCrashReportKey, value)
        }
        get() = kv.decodeBool(allowSendCrashReportKey, !BuildConfig.DEBUG)

    //夜间模式
    private val nightModeKey = "nightMode"
    var nightMode: NightMode
        set(value) {
            kv.encode(nightModeKey, value.value)
        }
        get() {
            val value = kv.decodeInt(nightModeKey, NightMode.AUTO.value)
            return NightMode.values().first { it.value == value }
        }

    //开发者模式
    private val debugModeKey = "debugMode"
    var debugMode: Boolean
        set(value) {
            kv.encode(debugModeKey, value)
        }
        get() = kv.decodeBool(debugModeKey, false)

    //始终显示崩溃信息
    private val alwaysCrashKey = "alwaysCrash"
    var alwaysCrash: Boolean
        set(value) {
            kv.encode(alwaysCrashKey, value)
        }
        get() = kv.decodeBool(alwaysCrashKey, debugMode)

    //自定义课表背景图
    private val backgroundImageKey = "backgroundImage"
    var backgroundImage: File?
        set(value) {
            if (value != null) {
                kv.encode(backgroundImageKey, value.absolutePath)
            } else {
                kv.remove(backgroundImageKey)
            }
        }
        get() {
            val image = kv.decodeString(backgroundImageKey)
            return if (image.isNullOrBlank()) null else File(image)
        }

    //自定义字体文件
    private val customFontFileKey = "customFontFile"
    var customFontFile: File?
        set(value) {
            kv.encode(customFontFileKey, value?.absolutePath)
        }
        get() {
            val save = kv.getString(customFontFileKey, null) ?: return null
            val file = File(save)
            if (!file.exists()) {
                return null
            }
            return file
        }
}