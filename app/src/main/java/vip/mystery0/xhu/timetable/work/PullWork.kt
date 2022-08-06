package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.packageName
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DEFAULT
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_PUSH
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import java.time.Instant

class PullWork(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val notificationManager: NotificationManager by inject()
    private val serverApi: ServerApi by inject()

    companion object {
        private const val TAG = "PullWork"
        private const val NOTIFICATION_TAG = "PullWork"
        private val NOTIFICATION_ID = NotificationId.PULL.id
    }

    override suspend fun doWork(): Result {
        startForeground()
        setConfig { pullWorkLastExecuteTime = Instant.now() }
        val user = SessionManager.loggedUserList().find { it.main } ?: return Result.success()
        val response = user.withAutoLogin {
            serverApi.fetchPush(it).checkLogin()
        }.first
        if (response.isEmpty()) {
            return Result.success()
        }
        var notificationIndex = getConfig { pushNotificationIndex }
        response.forEach {
            val title = it.title
            val content = it.content
            var actionType = ActionType.DO_NOTHING
            for (type in ActionType.values()) {
                if (it.operation.actionType == type.name) {
                    actionType = type
                    break
                }
            }
            val actionParams = it.operation.actionParams
            showPush(
                title,
                content,
                actionType,
                actionParams
            ).notify(NOTIFICATION_ID + notificationIndex)
            notificationIndex++
        }
        setConfig { pushNotificationIndex = notificationIndex }
        return Result.success()
    }

    private suspend fun startForeground() =
        setForeground(
            ForegroundInfo(
                NOTIFICATION_ID,
                silentNotificationBuilder
                    .setContentTitle("正在拉取信息")
                    .build()
            )
        )

    private val silentNotificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DEFAULT)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_push)
            .setOngoing(true)
            .setAutoCancel(true)

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_PUSH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_push)
            .setOngoing(false)
            .setAutoCancel(true)

    private fun showPush(
        title: String,
        content: String,
        actionType: ActionType,
        actionParams: List<String>,
    ): Notification {
        val pendingIntent = when (actionType) {
            ActionType.OPEN_URL -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(actionParams[0])).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                PendingIntent.getActivity(
                    appContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            ActionType.OPEN_ACTIVITY -> PendingIntent.getActivity(
                appContext,
                0,
                appContext.packageManager.getLaunchIntentForPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            else -> null
        }
        return notificationBuilder
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun Notification.notify(id: Int) {
        notificationManager.notify(NOTIFICATION_TAG, id, this)
    }
}

enum class ActionType {
    DO_NOTHING,
    OPEN_ACTIVITY,
    OPEN_URL,
}