package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.externalDownloadDir
import vip.mystery0.xhu.timetable.ui.activity.DownloadUpdateState
import vip.mystery0.xhu.timetable.ui.activity.addDownloadObserver
import vip.mystery0.xhu.timetable.ui.activity.formatFileSize
import vip.mystery0.xhu.timetable.ui.activity.removeDownloadObserver
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DOWNLOAD
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.sha256
import java.io.File
import java.io.FileOutputStream

class DownloadApkWork(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadApkWork"
        private const val NOTIFICATION_TAG = "DownloadNotification"
        private val NOTIFICATION_ID = NotificationId.DOWNLOAD.id
    }

    private val notificationManager: NotificationManager by inject()
    private val fileApi: FileApi by inject()
    private var versionName: String = appVersionName

    override suspend fun doWork(): Result {
        val version = DataHolder.version ?: return Result.success()
        val observerId = addDownloadObserver(patchObserver = false) {
            updateProgress(it)
        }
        val dir = File(externalDownloadDir, "apk")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${version.versionName}-${version.versionCode}".sha256())
        if (file.exists()) {
            file.delete()
        }
        startDownload()
        val response = fileApi.download(version.apkMd5)
        withContext(Dispatchers.IO) {
            response.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        Log.i(TAG, "save apk to ${file.absolutePath}")
        removeDownloadObserver(patchObserver = false, observerId)
        return Result.success()
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentTitle(
                context.getString(
                    R.string.download_notification_title_download,
                    versionName
                )
            )

    private fun startDownload() =
        notificationBuilder
            .build()
            .show()

    private fun updateProgress(downloadUpdateState: DownloadUpdateState) =
        notificationBuilder
            .setProgress(100, 0, false)
            .setContentText(
                context.getString(
                    R.string.download_notification_title_download_progress,
                    downloadUpdateState.downloaded.formatFileSize(),
                    downloadUpdateState.totalSize.formatFileSize(),
                )
            )
            .setSubText(
                context.getString(
                    R.string.download_notification_text,
                    downloadUpdateState.progress
                )
            )
            .build()
            .show()

    private fun Notification.show() {
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, this)
    }

    private fun dismiss() {
        notificationManager.cancel(NOTIFICATION_TAG, NOTIFICATION_ID)
    }
}
