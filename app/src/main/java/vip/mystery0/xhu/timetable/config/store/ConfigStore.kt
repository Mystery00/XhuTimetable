package vip.mystery0.xhu.timetable.config.store

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tencent.mmkv.MMKV
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.module.registerAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object Formatter {
    val DATE = DateTimeFormatter.ISO_LOCAL_DATE
    val TIME = DateTimeFormatter.ISO_LOCAL_TIME
}

private val instance = ConfigStore()
val GlobalNewConfig = instance

suspend fun <T> getConfigStore(block: ConfigStore.() -> T) = runOnIo { block(instance) }
suspend fun setConfigStore(block: suspend ConfigStore.() -> Unit) = runOnIo { block(instance) }

class ConfigStore internal constructor() {
    private val kv = MMKV.mmkvWithID("ConfigStore")
    private val moshi = Moshi.Builder().registerAdapter().build()

    private val userStoreSecretKey = "userStoreSecret"
    var userStoreSecret: String
        set(value) {
            kv.encode(userStoreSecretKey, value)
        }
        get() = kv.decodeString(userStoreSecretKey) ?: ""
    private val termStartDateKey = "termStartDate"
    val termStartDate: LocalDate
        get() = customTermStartDate.data
    var customTermStartDate: Customisable<LocalDate>
        set(value) {
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

    private val nowYearKey = "nowYear"
    val nowYear: Int
        get() = customNowYear.data
    var customNowYear: Customisable<Int>
        set(value) {
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

    private val nowTermKey = "nowTerm"
    val nowTerm: Int
        get() = customNowTerm.data
    var customNowTerm: Customisable<Int>
        set(value) {
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

    private val splashListMoshi = moshi.adapter<List<Splash>>(
        Types.newParameterizedType(
            List::class.java,
            Splash::class.java
        )
    )
    private val splashListKey = "splashList"
    var splashList: List<Splash>
        set(value) {
            kv.encode(splashListKey, splashListMoshi.toJson(value))
        }
        get() {
            val saveValue = kv.decodeString(splashListKey) ?: "[]"
            return splashListMoshi.fromJson(saveValue) ?: emptyList()
        }
    private val hideSplashBeforeKey = "hideSplashBefore"
    var hideSplashBefore: Instant
        set(value) {
            kv.encode(hideSplashBeforeKey, value.toEpochMilli())
        }
        get() {
            val time = kv.decodeLong(hideSplashBeforeKey, 0L)
            return Instant.ofEpochMilli(time)
        }
    private val showCustomCourseOnWeekKey = "showCustomCourseOnWeek"
    var showCustomCourseOnWeek: Boolean
        set(value) {
            kv.encode(showCustomCourseOnWeekKey, value)
        }
        get() = kv.decodeBool(showCustomCourseOnWeekKey, true)
    private val lastSyncCourseKey = "lastSyncCourse"
    var lastSyncCourse: LocalDate
        set(value) {
            kv.encode(lastSyncCourseKey, value.format(Formatter.DATE))
        }
        get() {
            val value = kv.decodeString(lastSyncCourseKey)
            if (value.isNullOrBlank()) {
                return LocalDate.MIN
            }
            return LocalDate.parse(value, Formatter.DATE)
        }
    private val multiAccountModeKey = "multiAccountMode"
    var multiAccountMode: Boolean
        set(value) {
            kv.encode(multiAccountModeKey, value)
        }
        get() = kv.decodeBool(multiAccountModeKey, false)
    private val showNotThisWeekKey = "showNotThisWeek"
    var showNotThisWeek: Boolean
        set(value) {
            kv.encode(showNotThisWeekKey, value)
        }
        get() = kv.decodeBool(showNotThisWeekKey, true)
    private val showTomorrowCourseTimeKey = "showTomorrowCourseTime"
    var showTomorrowCourseTime: LocalTime?
        set(value) {
            kv.encode(showTomorrowCourseTimeKey, value?.format(Formatter.TIME))
        }
        get() = kv.decodeString(showTomorrowCourseTimeKey)
            ?.let { LocalTime.parse(it, Formatter.TIME) }
    private val showStatusKey = "showStatus"
    var showStatus: Boolean
        set(value) {
            kv.encode(showStatusKey, value)
        }
        get() = kv.decodeBool(showStatusKey, true)
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
    private val versionChannelKey = "versionChannel"
    var versionChannel: VersionChannel
        set(value) {
            kv.encode(versionChannelKey, value.value)
        }
        get() {
            val save = kv.decodeInt(versionChannelKey, VersionChannel.STABLE.value)
            return VersionChannel.parse(save)
        }
}