package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.BaseCoroutineWorker
import vip.mystery0.xhu.timetable.base.DownloadError
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.externalDownloadDir
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.packageName
import vip.mystery0.xhu.timetable.ui.activity.DownloadUpdateState
import vip.mystery0.xhu.timetable.ui.activity.addDownloadObserver
import vip.mystery0.xhu.timetable.ui.activity.formatFileSize
import vip.mystery0.xhu.timetable.ui.activity.removeDownloadObserver
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DOWNLOAD
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.md5
import java.io.File
import java.io.FileOutputStream

class DownloadApkWork(private val appContext: Context, workerParams: WorkerParameters) :
    BaseCoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadApkWork"
        private const val NOTIFICATION_TAG = "DownloadApkWork"
        private val NOTIFICATION_ID = NotificationId.DOWNLOAD.id
    }

    private val notificationManager: NotificationManager by inject()
    private val serverApi: ServerApi by inject()
    private val fileApi: FileApi by inject()

    override suspend fun doWork(): Result {
        val version = DataHolder.version ?: return Result.success()
        val observerId = addDownloadObserver(patchObserver = false) {
            setForeground(updateProgress(it))
        }
        val dir = File(externalDownloadDir, "apk")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${version.versionName}-${version.versionCode}.apk")
        if (file.exists()) {
            file.delete()
        }
        setForeground(startDownload(version))
        //获取下载地址
        val versionUrl = serverApi.versionUrl(version.versionId)
        val response = fileApi.download(versionUrl.apkUrl)
        runOnIo {
            response.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        removeDownloadObserver(patchObserver = false, observerId)
        Log.i(TAG, "save apk to ${file.absolutePath}")
        //检查md5
        setForeground(md5Checking())
        val md5 = file.md5()
        if (md5 != versionUrl.apkMd5) {
            throw DownloadError.MD5CheckFailed()
        }
        //md5校验通过，安装应用
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val uri = FileProvider.getUriForFile(appContext, packageName, file)
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
        appContext.startActivity(installIntent)
        return Result.success()
    }

    override suspend fun whenError(t: Throwable) {
        super.whenError(t)
        val notification = when (t) {
            is DownloadError.MD5CheckFailed -> md5Failed()
            else -> {
                Log.w(TAG, "download apk failed", t)
                downloadFailed()
            }
        }
        notification.notify()
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentText("正在获取下载地址……")

    private val failedNotificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download_failed)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentText(null)

    private fun startDownload(version: Version): ForegroundInfo =
        ForegroundInfo(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentTitle("正在下载：${version.versionName}")
                .build()
        )


    private fun updateProgress(downloadUpdateState: DownloadUpdateState): ForegroundInfo =
        ForegroundInfo(
            NOTIFICATION_ID,
            notificationBuilder
                .setProgress(100, 0, false)
                .setContentText("${downloadUpdateState.downloaded.formatFileSize()}/${downloadUpdateState.totalSize.formatFileSize()}")
                .setSubText("已下载${downloadUpdateState.progress}%")
                .build()
        )

    private fun md5Checking(): ForegroundInfo =
        ForegroundInfo(
            NOTIFICATION_ID,
            notificationBuilder
                .setProgress(0, 0, false)
                .setContentTitle("MD5校验中……")
                .setContentText(null)
                .setOngoing(false)
                .build()
        )

    private fun md5Failed(): Notification =
        failedNotificationBuilder
            .setContentTitle("MD5校验失败，请重新下载")
            .build()

    private fun downloadFailed() =
        failedNotificationBuilder
            .setContentTitle("下载失败")
            .build()

    private fun Notification.notify() {
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, this)
    }
}
