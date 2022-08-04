package vip.mystery0.xhu.timetable.config

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tencent.mmkv.MMKV
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.response.Menu
import vip.mystery0.xhu.timetable.model.response.Splash
import java.io.File
import java.time.*
import java.time.format.DateTimeFormatter

val chinaZone: ZoneId = ZoneId.of("Asia/Shanghai")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

private val instance = Config()
val GlobalConfig = instance

suspend fun <T> getConfig(block: Config.() -> T) = runOnIo { block(instance) }
suspend fun setConfig(block: suspend Config.() -> Unit) = runOnIo { block(instance) }

class Config internal constructor() {
    private val kv = MMKV.defaultMMKV()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    var firstEnter: Boolean
        set(value) {
            kv.encode("firstEnter", value)
        }
        get() = kv.decodeBool("firstEnter", true)

    var lastVersionCode: Long
        set(value) {
            kv.encode("lastVersionCode", value)
        }
        get() = kv.decodeLong("lastVersionCode")

    var nightMode: NightMode
        set(value) {
            kv.encode("nightMode", value.value)
        }
        get() {
            val value = kv.decodeInt("nightMode", NightMode.AUTO.value)
            return NightMode.values().first { it.value == value }
        }

    var ignoreVersionList: HashSet<String>
        set(value) {
            kv.encode("ignoreVersionList", value)
        }
        get() {
            val set = kv.decodeStringSet("ignoreVersionList") ?: emptySet()
            return HashSet(set)
        }

    var customTermStartTime: Pair<Instant, Boolean>
        set(value) {
            kv.encode(
                if (value.second) "customTermStartTime" else "termStartTime",
                value.first.toEpochMilli(),
            )
        }
        get() {
            val customTime = kv.decodeLong("customTermStartTime", 0L)
            if (customTime != 0L) {
                return Instant.ofEpochMilli(customTime) to true
            }
            val time = kv.decodeLong("termStartTime", 0L)
            return Instant.ofEpochMilli(time) to false
        }
    val termStartTime: Instant
        get() = customTermStartTime.first

    var currentYearData: Pair<String, Boolean>
        set(value) {
            kv.encode("currentYear", if (value.second) value.first else "")
        }
        get() {
            val customYear = kv.decodeString("currentYear", "")
            if (!customYear.isNullOrBlank()) {
                return customYear to true
            }
            val time = LocalDateTime.ofInstant(termStartTime, chinaZone)
            val year = if (time.month < Month.JUNE) {
                "${time.year - 1}-${time.year}"
            } else {
                "${time.year}-${time.year + 1}"
            }
            return year to false
        }
    val currentYear: String
        get() = currentYearData.first

    var currentTermData: Pair<Int, Boolean>
        set(value) {
            kv.encode("currentTerm", if (value.second) value.first else -1)
        }
        get() {
            val customTerm = kv.decodeInt("currentTerm", -1)
            if (customTerm != -1) {
                return customTerm to true
            }
            val time = LocalDateTime.ofInstant(termStartTime, chinaZone)
            val term = if (time.month < Month.JUNE) {
                2
            } else {
                1
            }
            return term to false
        }
    val currentTerm: Int
        get() = currentTermData.first

    var splashList: List<Splash>
        set(value) {
            kv.encode(
                "splashList",
                moshi.adapter<List<Splash>>(
                    Types.newParameterizedType(
                        List::class.java,
                        Splash::class.java
                    )
                ).toJson(value)
            )
        }
        get() = moshi.adapter<List<Splash>>(
            Types.newParameterizedType(
                List::class.java,
                Splash::class.java
            )
        ).fromJson(kv.decodeString("splashList", "[]")!!)!!

    var userList: List<User>
        set(value) {
            kv.encode(
                "userList",
                moshi.adapter<List<User>>(
                    Types.newParameterizedType(
                        List::class.java,
                        User::class.java
                    )
                ).toJson(value)
            )
        }
        get() = moshi.adapter<List<User>>(
            Types.newParameterizedType(
                List::class.java,
                User::class.java
            )
        ).fromJson(kv.decodeString("userList", "[]")!!)!!
    var poemsToken: String?
        set(value) {
            kv.encode("poemsToken", value)
        }
        get() = kv.decodeString("poemsToken")
    var lastSyncCourse: LocalDate
        set(value) {
            kv.encode(
                "lastSyncCourse",
                value.atStartOfDay().atZone(chinaZone).toInstant().toEpochMilli()
            )
        }
        get() {
            val decodeLong = kv.decodeLong("lastSyncCourse")
            return if (decodeLong != 0L) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(decodeLong), chinaZone).toLocalDate()
            } else {
                LocalDate.MIN
            }
        }
    var lastSyncNotice: LocalDate
        set(value) {
            kv.encode(
                "lastSyncNotice",
                value.atStartOfDay().atZone(chinaZone).toInstant().toEpochMilli()
            )
        }
        get() {
            val decodeLong = kv.decodeLong("lastSyncNotice")
            return if (decodeLong != 0L) {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(decodeLong), chinaZone).toLocalDate()
            } else {
                LocalDate.MIN
            }
        }
    var backgroundImage: File?
        set(value) {
            if (value != null) {
                kv.encode("backgroundImage", value.absolutePath)
            } else {
                kv.remove("backgroundImage")
            }
        }
        get() {
            val image = kv.decodeString("backgroundImage")
            return if (image.isNullOrBlank()) null else File(image)
        }
    var multiAccountMode: Boolean
        set(value) {
            kv.encode("multiAccountMode", value)
        }
        get() = kv.decodeBool("multiAccountMode", false)
    var showStatus: Boolean
        set(value) {
            kv.encode("showStatus", value)
        }
        get() = kv.decodeBool("showStatus", true)
    var showNotThisWeek: Boolean
        set(value) {
            kv.encode("showNotThisWeek", value)
        }
        get() = kv.decodeBool("showNotThisWeek", true)
    var showTomorrowCourseTime: LocalTime?
        set(value) {
            kv.encode("showTomorrowCourseTime", value?.format(timeFormatter))
        }
        get() = kv.decodeString("showTomorrowCourseTime")
            ?.let { LocalTime.parse(it, timeFormatter) }
    var notifyCourse: Boolean
        set(value) {
            kv.encode("notifyCourse", value)
        }
        get() = kv.decodeBool("notifyCourse", true)
    var notifyExam: Boolean
        set(value) {
            kv.encode("notifyExam", value)
        }
        get() = kv.decodeBool("notifyExam", true)
    var notifyTime: LocalTime?
        set(value) {
            kv.encode("notifyTime", value?.format(timeFormatter))
        }
        get() = kv.decodeString("notifyTime")
            ?.let { LocalTime.parse(it, timeFormatter) }
    var disablePoems: Boolean
        set(value) {
            kv.encode("disablePoems", value)
        }
        get() = kv.decodeBool("disablePoems", false)
    var showPoemsTranslate: Boolean
        set(value) {
            kv.encode("showPoemsTranslate", value)
        }
        get() = kv.decodeBool("showPoemsTranslate", true)
    var allowSendCrashReport: Boolean
        set(value) {
            kv.encode("allowSendCrashReport", value)
        }
        get() = kv.decodeBool("allowSendCrashReport", !BuildConfig.DEBUG)
    var debugMode: Boolean
        set(value) {
            kv.encode("debugMode", value)
        }
        get() = kv.decodeBool("debugMode", false)
    var alwaysShowNewVersion: Boolean
        set(value) {
            kv.encode("alwaysShowNewVersion", value)
        }
        get() = kv.decodeBool("alwaysShowNewVersion", false)
    var showCustomCourseOnWeek: Boolean
        set(value) {
            kv.encode("showCustomCourseOnWeek", value)
        }
        get() = kv.decodeBool("showCustomCourseOnWeek", true)
    var showCustomThing: Boolean
        set(value) {
            kv.encode("showCustomThing", value)
        }
        get() = kv.decodeBool("showCustomThing", true)
    var firstFeedbackMessageId: Long
        set(value) {
            kv.encode("firstFeedbackMessageId", value)
        }
        get() = kv.decodeLong("firstFeedbackMessageId", 0L)
    var disableBackgroundWhenNight: Boolean
        set(value) {
            kv.encode("disableBackgroundWhenNight", value)
        }
        get() = kv.decodeBool("disableBackgroundWhenNight", true)
    var menuList: List<Menu>
        set(value) {
            kv.encode(
                "menuList",
                moshi.adapter<List<Menu>>(
                    Types.newParameterizedType(
                        List::class.java,
                        Menu::class.java
                    )
                ).toJson(value)
            )
        }
        get() = moshi.adapter<List<Menu>>(
            Types.newParameterizedType(
                List::class.java,
                Menu::class.java
            )
        ).fromJson(kv.decodeString("menuList", "[]")!!)!!
    var hideSplashBefore: Instant
        set(value) {
            kv.encode("hideSplashBefore", value.toEpochMilli())
        }
        get() {
            val time = kv.decodeLong("hideSplashBefore", 0L)
            return Instant.ofEpochMilli(time)
        }
    var customUi: CustomUi
        set(value) {
            kv.encode("customUi", moshi.adapter(CustomUi::class.java).toJson(value))
        }
        get() {
            val save = kv.decodeString("customUi", "")
            return if (save.isNullOrBlank()) CustomUi.DEFAULT
            else moshi.adapter(CustomUi::class.java).fromJson(save)!!
        }
    var pushNotificationIndex: Int
        set(value) {
            kv.encode("pushNotificationIndex", value)
        }
        get() = kv.decodeInt("pushNotificationIndex", 1)
    var pullWorkLastExecuteTime: Instant
        set(value) {
            kv.encode("pullWorkLastExecuteTime", value.toEpochMilli())
        }
        get() {
            val time = kv.decodeLong("pullWorkLastExecuteTime", 0L)
            return Instant.ofEpochMilli(time)
        }
    var notifyWorkLastExecuteTime: Instant
        set(value) {
            kv.encode("notifyWorkLastExecuteTime", value.toEpochMilli())
        }
        get() {
            val time = kv.decodeLong("notifyWorkLastExecuteTime", 0L)
            return Instant.ofEpochMilli(time)
        }
    var autoCacheJwcCourse: Boolean
        set(value) {
            kv.encode("autoCacheJwcCourse", value)
        }
        get() = kv.decodeBool("autoCacheJwcCourse", true)
    var showOldCourseWhenFailed: Boolean
        set(value) {
            kv.encode("showOldCourseWhenFailed", value)
        }
        get() = kv.decodeBool("showOldCourseWhenFailed", true)
}