package vip.mystery0.xhu.timetable.work

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DEFAULT
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.asInstant
import java.time.LocalDate

class NotifyService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private suspend fun doWork() {
        NotifySetter.lock {
            val action = NotifyAction(this)
            runCatching {
                action.checkNotifyCourse()
            }
            runCatching {
                action.checkNotifyExam()
            }
        }
        return complete()
    }

    private suspend fun complete() {
        getConfigStore { notifyTime }?.let {
            NotifySetter.setAlarmTrigger(it.atDate(LocalDate.now().plusDays(1)).asInstant())
        }
    }

    override fun onCreate() {
        super.onCreate()
        val notification =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_DEFAULT)
                .setSmallIcon(R.drawable.ic_stat_init)
                .setContentText("正在初始化数据")
                .setAutoCancel(true)
                .setPriority(NotificationManagerCompat.IMPORTANCE_NONE)
                .build()
        startForeground(NotificationId.NOTIFY_TOMORROW_FOREGROUND.id, notification)
        scope.launch {
            doWork()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        job.cancel()
    }
}