package vip.mystery0.xhu.timetable.config.store

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.module.registerAdapter
import java.time.Instant
import java.time.LocalDate


private val instance = CacheStore()

suspend fun <T> getCacheStore(block: CacheStore.() -> T) =
    withContext(Dispatchers.IO) { block(instance) }

suspend fun setCacheStore(block: suspend CacheStore.() -> Unit) =
    withContext(Dispatchers.IO) { block(instance) }

class CacheStore {
    private val kv = MMKV.mmkvWithID("CacheStore")
    private val moshi = Moshi.Builder().registerAdapter().build()

    //启动图
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

    //上一次同步课表的时间
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

    //接收到的最新通知的id
    private val latestNoticeIdKey = "latestNoticeId"
    var lastNoticeId: Int
        set(value) {
            kv.encode(latestNoticeIdKey, value)
        }
        get() = kv.decodeInt(latestNoticeIdKey, 0)

    //课程提醒上一次执行时间
    private val notifyWorkLastExecuteTimeKey = "notifyWorkLastExecuteTime"
    var notifyWorkLastExecuteTime: Instant
        set(value) {
            kv.encode(notifyWorkLastExecuteTimeKey, value.toEpochMilli())
        }
        get() {
            val time = kv.decodeLong(notifyWorkLastExecuteTimeKey, 0L)
            return Instant.ofEpochMilli(time)
        }
}