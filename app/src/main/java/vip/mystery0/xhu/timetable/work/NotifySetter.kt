package vip.mystery0.xhu.timetable.work

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
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
    private val alarmManager: AlarmManager by inject()
    private val workManager: WorkManager by inject()

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
        Log.i(
            "ApplicationExt",
            "set alarm trigger success, next execute time: ${nextExecuteTime.asLocalDateTime()}",
        )
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

        val nextExecuteTime = calculateNextExecuteTime(executeTime) ?: return

        val duration = Duration.between(Instant.now(), nextExecuteTime)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<NotifyWork>()
                .setInitialDelay(duration.toMillis(), TimeUnit.MILLISECONDS)
                .build()
        )
        Log.i(
            "ApplicationExt",
            "work enqueue success, next execute time: ${nextExecuteTime.asLocalDateTime()}",
        )
    }
}