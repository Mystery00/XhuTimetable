package vip.mystery0.xhu.timetable.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atDate
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.between
import vip.mystery0.xhu.timetable.utils.now
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

object NotifySetter : KoinComponent {
    private const val TAG = "NotifySetter"
    private val mutex = Mutex()
    private val alarmManager: AlarmManager by inject()
    private val workManager: WorkManager by inject()

    suspend fun <T> lock(action: suspend () -> T) {
        runCatching {
            mutex.withLock(this) {
                val lastDate = getCacheStore { notifyWorkLastExecuteDate }
                if (lastDate == LocalDate.now()) {
                    return
                }
                setCacheStore { notifyWorkLastExecuteDate = LocalDate.now() }
                setCacheStore { notifyWorkLastExecuteTime = Clock.System.now() }
                action()
            }
        }
    }

    suspend fun setTrigger(executeTime: Instant? = null) {
        //清空上一次执行时间
        setCacheStore { notifyWorkLastExecuteDate = LocalDate.MIN }
        setAlarmTrigger(executeTime)
        setWorkTrigger(executeTime)
    }

    private suspend fun calculateNextExecuteTime(executeTime: Instant?): Instant? {
        var nextExecuteTime = executeTime?.asLocalDateTime()
            ?: getConfigStore { notifyTime }?.atDate(LocalDate.now())
            ?: return null
        if (nextExecuteTime < LocalDateTime.now()) {
            //当天计算出来的时间比当前时间早，那么调度时间改成明天
            nextExecuteTime = nextExecuteTime.date
                .plus(1, DateTimeUnit.DAY)
                .atTime(nextExecuteTime.time)
        }
        return nextExecuteTime.asInstant()
    }

    suspend fun setAlarmTrigger(executeTime: Instant? = null) {
        val notifyCourse = getConfigStore { notifyCourse }
        val notifyExam = getConfigStore { notifyExam }
        if (!notifyCourse && !notifyExam) {
            return
        }
        val alarmIntent = Intent(context, NotifyService::class.java)
        val pendingIntent = PendingIntent.getForegroundService(
            context,
            0,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        //关闭定时器
        alarmManager.cancel(pendingIntent)

        val nextExecuteTime = calculateNextExecuteTime(executeTime) ?: return

        alarmManager.cancel(pendingIntent)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            nextExecuteTime.toEpochMilliseconds(),
            pendingIntent
        )
        Log.i(TAG, "set alarm success, next time: ${nextExecuteTime.asLocalDateTime()}")
    }

    suspend fun setWorkTrigger(executeTime: Instant? = null) {
        val notifyCourse = getConfigStore { notifyCourse }
        val notifyExam = getConfigStore { notifyExam }
        if (!notifyCourse && !notifyExam) {
            return
        }
        val uniqueWorkName = NotifyWork::class.java.name
        if (executeTime == null) {
            workManager.cancelUniqueWork(uniqueWorkName)
        }

        var nextExecuteTime = calculateNextExecuteTime(executeTime) ?: return
        //避免因为相同时间的触发导致的重复执行
        nextExecuteTime = nextExecuteTime.plus(1, DateTimeUnit.MINUTE)

        val duration = Duration.between(Clock.System.now(), nextExecuteTime)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<NotifyWork>()
                .setInitialDelay(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                .build()
        )
        Log.i(TAG, "work enqueue success, next time: ${nextExecuteTime.asLocalDateTime()}")
    }
}