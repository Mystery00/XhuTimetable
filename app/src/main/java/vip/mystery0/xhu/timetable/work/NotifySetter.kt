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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

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
                setCacheStore { notifyWorkLastExecuteTime = Instant.now() }
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
        var nextExecuteTime = executeTime
            ?: getConfigStore { notifyTime }?.atDate(LocalDate.now())?.asInstant()
            ?: return null
        if (nextExecuteTime.isBefore(Instant.now())) {
            //当天计算出来的时间比当前时间早，那么调度时间改成明天
            nextExecuteTime = nextExecuteTime.plus(1, ChronoUnit.DAYS)
        }
        return nextExecuteTime
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
            nextExecuteTime.toEpochMilli(),
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
        nextExecuteTime = nextExecuteTime.plus(1, ChronoUnit.MINUTES)

        val duration = Duration.between(Instant.now(), nextExecuteTime)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<NotifyWork>()
                .setInitialDelay(duration.toMillis(), TimeUnit.MILLISECONDS)
                .build()
        )
        Log.i(TAG, "work enqueue success, next time: ${nextExecuteTime.asLocalDateTime()}")
    }
}