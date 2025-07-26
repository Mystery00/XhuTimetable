package vip.mystery0.xhu.timetable.config.store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.model.response.Holiday
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.now

private val instance = CacheStore()
val GlobalCacheStore = instance

suspend fun <T> getCacheStore(block: CacheStore.() -> T) =
    withContext(Dispatchers.IO) { block(instance) }

suspend fun setCacheStore(block: suspend CacheStore.() -> Unit) =
    withContext(Dispatchers.IO) { block(instance) }

class CacheStore {
    private val json = Json { ignoreUnknownKeys = true }

    //启动图
    private val splashListKey = "splashList"
    var splashList: List<Splash>
        set(value) {
            Store.CacheStore.setConfiguration(splashListKey, json.encodeToString(value))
        }
        get() {
            val saveValue = Store.CacheStore.getConfiguration(splashListKey, "[]")
            return json.decodeFromString(saveValue)
        }

    //上一次同步课表的时间
    private val lastSyncCourseKey = "lastSyncCourse"
    var lastSyncCourse: LocalDate
        set(value) {
            Store.CacheStore.setConfiguration(
                lastSyncCourseKey,
                value.format(Formatter.DATE)
            )
        }
        get() {
            val value = Store.CacheStore.getConfiguration(lastSyncCourseKey, "")
            if (value.isBlank()) {
                return LocalDate(2000, 1, 1)
            }
            return LocalDate.parse(value, Formatter.DATE)
        }

    //接收到的最新通知的id
    private val latestNoticeIdKey = "latestNoticeId"
    var lastNoticeId: Int
        set(value) {
            Store.CacheStore.setConfiguration(latestNoticeIdKey, value)
        }
        get() = Store.CacheStore.getConfiguration(latestNoticeIdKey, 0)

    //课程提醒上一次执行时间
    private val notifyWorkLastExecuteTimeKey = "notifyWorkLastExecuteTime"
    var notifyWorkLastExecuteTime: Instant
        set(value) {
            Store.CacheStore.setConfiguration(
                notifyWorkLastExecuteTimeKey,
                value.toEpochMilliseconds()
            )
        }
        get() {
            val time = Store.CacheStore.getConfiguration(notifyWorkLastExecuteTimeKey, 0L)
            return Instant.fromEpochMilliseconds(time)
        }

    //课程提醒上一次执行日期，用来判断有没有重复执行
    private val notifyWorkLastExecuteDateKey = "notifyWorkLastExecuteDate"
    var notifyWorkLastExecuteDate: LocalDate
        set(value) {
            if (value == LocalDate.MIN) {
                Store.CacheStore.removeConfiguration(notifyWorkLastExecuteDateKey)
                return
            }
            Store.CacheStore.setConfiguration(
                notifyWorkLastExecuteDateKey,
                value.format(Formatter.DATE)
            )
        }
        get() {
            val date = Store.CacheStore.getConfiguration(notifyWorkLastExecuteDateKey, "")
            if (date.isBlank()) {
                return LocalDate.MIN
            }
            return LocalDate.parse(date, Formatter.DATE)
        }

    //隐藏启动图到指定日期
    private val hideSplashBeforeKey = "hideSplashBefore"
    var hideSplashBefore: LocalDate
        set(value) {
            if (value == LocalDate.MIN) {
                Store.CacheStore.removeConfiguration(hideSplashBeforeKey)
                return
            }
            val saveValue = value.format(Formatter.DATE)
            Store.CacheStore.setConfiguration(hideSplashBeforeKey, saveValue)
        }
        get() {
            val date = Store.CacheStore.getConfiguration(hideSplashBeforeKey, "")
            if (date.isBlank()) {
                return LocalDate.MIN
            }
            return LocalDate.parse(date, Formatter.DATE)
        }

    //始终显示新版本提醒
    private val alwaysShowNewVersionKey = "alwaysShowNewVersion"
    var alwaysShowNewVersion: Boolean
        set(value) {
            Store.CacheStore.setConfiguration(alwaysShowNewVersionKey, value)
        }
        get() = Store.CacheStore.getConfiguration(alwaysShowNewVersionKey, false)

    //忽略的版本
    private val ignoreVersionListKey = "ignoreVersionList"
    var ignoreVersionList: Set<String>
        set(value) {
            Store.CacheStore.setConfiguration(ignoreVersionListKey, value)
        }
        get() = Store.CacheStore.getConfiguration(ignoreVersionListKey, emptySet())

    //意见反馈的消息id
    private val firstFeedbackMessageIdKey = "firstFeedbackMessageId"
    var firstFeedbackMessageId: Long
        set(value) {
            Store.CacheStore.setConfiguration(firstFeedbackMessageIdKey, value)
        }
        get() = Store.CacheStore.getConfiguration(firstFeedbackMessageIdKey, 0L)

    //团队成员列表
    private val teamMemberListKey = "teamMemberList"
    var teamMemberList: List<TeamMemberResponse>
        set(value) {
            Store.CacheStore.setConfiguration(teamMemberListKey, json.encodeToString(value))
        }
        get() {
            val saveValue = Store.CacheStore.getConfiguration(teamMemberListKey, "[]")
            return json.decodeFromString(saveValue)
        }

    private val holidayKeyPrefix = "holiday:"
    var holiday: Pair<Holiday?, Holiday?>
        set(value) {
            val now = LocalDate.now()
            val tomorrow = now.plus(1, DateTimeUnit.DAY)
            if (value.first == null) {
                Store.CacheStore.removeConfiguration("$holidayKeyPrefix${now.format(Formatter.DATE)}")
            }
            if (value.second == null) {
                Store.CacheStore.removeConfiguration("$holidayKeyPrefix${tomorrow.format(Formatter.DATE)}")
            }
            value.first?.let {
                Store.CacheStore.setConfiguration(
                    "$holidayKeyPrefix${it.date.format(Formatter.DATE)}",
                    json.encodeToString(it),
                )
            }
            value.second?.let {
                Store.CacheStore.setConfiguration(
                    "$holidayKeyPrefix${it.date.format(Formatter.DATE)}",
                    json.encodeToString(it),
                )
            }
        }
        get() {
            val now = LocalDate.now()
            val tomorrow = now.plus(1, DateTimeUnit.DAY)
            val nowHoliday = Store.CacheStore.getConfiguration(
                "$holidayKeyPrefix${now.format(Formatter.DATE)}",
                ""
            )
            val tomorrowHoliday =
                Store.CacheStore.getConfiguration(
                    "$holidayKeyPrefix${tomorrow.format(Formatter.DATE)}",
                    ""
                )
            var first: Holiday? = null
            var second: Holiday? = null
            if (nowHoliday.isNotBlank()) {
                first = json.decodeFromString(nowHoliday)
            }
            if (tomorrowHoliday.isNotBlank()) {
                second = json.decodeFromString(tomorrowHoliday)
            }
            return Pair(first, second)
        }
}