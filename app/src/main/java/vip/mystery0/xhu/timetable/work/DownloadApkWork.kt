package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
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
import vip.mystery0.xhu.timetable.base.DownloadError
import vip.mystery0.xhu.timetable.base.XhuCoroutineWorker
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.interceptor.DownloadProgressInterceptor
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.externalCacheDownloadDir
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.packageName
import vip.mystery0.xhu.timetable.ui.activity.*
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DOWNLOAD
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.md5
import java.io.File
import java.io.FileOutputStream

class DownloadApkWork(private val appContext: Context, workerParams: WorkerParameters) :
    XhuCoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadApkWork"
        private const val NOTIFICATION_TAG = "DownloadApkWork"
        private val NOTIFICATION_ID = NotificationId.DOWNLOAD.id
    }

    private val notificationManager: NotificationManager by inject()
    private val serverApi: ServerApi by inject()
    private val fileApi: FileApi by inject()

    private var lastUpdateProgressTime = 0L

    private val observerId = addDownloadObserver(patchObserver = false) {
        val now = SystemClock.uptimeMillis()
        if (now - lastUpdateProgressTime > 100) {
            setForeground(updateProgress(it))
            lastUpdateProgressTime = now
        }
    }

    override suspend fun doWork(): Result {
        val version = DataHolder.version ?: return Result.success()
        startForeground(version)
        val dir = File(externalCacheDownloadDir, "apk")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${version.versionName}-${version.versionCode}.apk")
        if (file.exists()) {
            file.delete()
        }
        setForeground(getDownloadUrl(version))
        //获取下载地址
        val versionUrl = serverApi.versionUrl(version.versionId)
        val md5 = runOnIo {
            val response =
                fileApi.download(versionUrl.apkUrl, DownloadProgressInterceptor.buildTag(false))
            Log.i(TAG, "save apk to ${file.absolutePath}")
            response.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            //检查md5
            setForeground(md5Checking())
            file.md5()
        }
        updateStatus(status = "文件处理中", patch = false, progress = 100)
        if (!md5.equals(versionUrl.apkMd5, ignoreCase = true)) {
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

    override fun whenError(t: Throwable) {
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

    override fun onStopped() {
        super.onStopped()
        removeDownloadObserver(patchObserver = false, observerId)
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download)
            .setOngoing(true)
            .setAutoCancel(true)

    private val failedNotificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download_failed)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentText(null)

    private suspend fun startForeground(version: Version) =
        setForeground(
            ForegroundInfo(
                NOTIFICATION_ID,
                notificationBuilder
                    .setContentTitle("正在下载：${version.versionName}")
                    .setContentText("正在开始下载")
                    .build()
            )
        )

    private fun getDownloadUrl(version: Version): ForegroundInfo =
        ForegroundInfo(
            NOTIFICATION_ID,
            notificationBuilder
                .setContentTitle("正在下载：${version.versionName}")
                .setContentText("正在获取下载地址……")
                .build()
        )

    private fun updateProgress(downloadUpdateState: DownloadUpdateState): ForegroundInfo =
        ForegroundInfo(
            NOTIFICATION_ID,
            notificationBuilder
                .setProgress(100, downloadUpdateState.progress, false)
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
