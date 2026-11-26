package vip.mystery0.xhu.timetable.work

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.WorkerParameters
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.shared.R
import vip.mystery0.xhu.timetable.base.DownloadError
import vip.mystery0.xhu.timetable.base.XhuCoroutineWorker
import vip.mystery0.xhu.timetable.base.packageName
import vip.mystery0.xhu.timetable.config.externalCacheDownloadDir
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_FILE
import vip.mystery0.xhu.timetable.module.downloadFileTo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.ui.component.DownloadUpdateState
import vip.mystery0.xhu.timetable.ui.component.addDownloadObserver
import vip.mystery0.xhu.timetable.ui.component.removeDownloadObserver
import vip.mystery0.xhu.timetable.ui.component.updateObserverProgress
import vip.mystery0.xhu.timetable.ui.component.updateStatus
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DOWNLOAD
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.formatFileSize
import vip.mystery0.xhu.timetable.utils.md5
import java.io.File

class DownloadApkWork(private val appContext: Context, workerParams: WorkerParameters) :
    XhuCoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadApkWork"
        private const val NOTIFICATION_TAG = "DownloadApkWork"
        private val NOTIFICATION_ID = NotificationId.DOWNLOAD.id

        const val ARG_VERSION_ID = "versionId"
        const val ARG_VERSION_NAME = "versionName"
        const val ARG_VERSION_CODE = "versionCode"
        const val ARG_VERSION_CHECK_MD5 = "versionCheckMd5"
        const val ARG_FILE_SIZE = "fileSize"
    }

    private val fileClient: HttpClient by inject(named(HTTP_CLIENT_FILE))

    private var lastUpdateProgressTime = 0L

    private val observerId = addDownloadObserver(patchObserver = false) {
        val now = SystemClock.uptimeMillis()
        if (now - lastUpdateProgressTime > 100) {
            updateProgress(it).notify()
            lastUpdateProgressTime = now
        }
    }

    override suspend fun doWork(): Result {
        val versionId = inputData.getString(ARG_VERSION_ID)?.toLongOrNull() ?: return Result.failure()
        val versionName = inputData.getString(ARG_VERSION_NAME) ?: return Result.failure()
        val versionCode = inputData.getString(ARG_VERSION_CODE)?.toIntOrNull()
            ?: inputData.getInt(ARG_VERSION_CODE, 0)
        val versionCheckMd5 =
            inputData.getString(ARG_VERSION_CHECK_MD5)?.toBoolean() ?: return Result.failure()
        val expectedFileSize = inputData.getLong(ARG_FILE_SIZE, 0L)
        startDownload(versionName).notify()
        val file = withContext(Dispatchers.IO) {
            val dir = PlatformFile(externalCacheDownloadDir, "apk")
            if (!dir.exists()) {
                dir.createDirectories(true)
            }
            val file = PlatformFile(dir, "${versionName}-${versionCode}.apk")
            if (file.exists()) {
                file.delete()
            }
            file
        }
        getDownloadUrl(versionName).notify()

        //获取下载地址
        updateStatus(status = "正在获取下载地址...", patch = false, progress = 0, indeterminate = true)
        val versionUrl = StartRepo.getVersionUrl(versionId)

        Log.i(TAG, "save apk to ${file.absolutePath()}")
        updateStatus(status = "开始下载安装包...", patch = false, progress = 0, indeterminate = true)
        withContext(Dispatchers.IO) {
            fileClient.downloadFileTo(
                saveFile = file,
                builder = {
                    url(versionUrl.apkUrl)
                },
                progress = { progress ->
                    val total = if (progress.total > 0L) progress.total else expectedFileSize
                    val percent = if (total > 0L) {
                        (progress.received * 100L / total).toInt().coerceIn(0, 100)
                    } else {
                        0
                    }
                    val statusText = if (total > 0L) {
                        "已下载 ${progress.received.formatFileSize()} / ${total.formatFileSize()}"
                    } else {
                        "已下载 ${progress.received.formatFileSize()}"
                    }
                    val state = DownloadUpdateState(
                        downloading = true,
                        indeterminate = total <= 0L,
                        downloaded = progress.received,
                        totalSize = total,
                        patch = false,
                        progress = percent,
                        status = statusText,
                    )
                    updateObserverProgress(state)
                }
            )
        }
        updateStatus(status = "正在校验安装包...", patch = false, progress = 100, indeterminate = true)
        md5Checking().notify()
        //检查md5
        val md5 = withContext(Dispatchers.Default) {
            file.md5()
        }
        updateStatus(status = "文件处理中...", patch = false, progress = 100, indeterminate = true)
        if (versionCheckMd5 && !md5.equals(versionUrl.apkMd5, ignoreCase = true)) {
            throw DownloadError.MD5CheckFailed()
        }
        //md5校验通过，安装应用
        val installIntent = Intent(Intent.ACTION_VIEW)
        installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val uri = FileProvider.getUriForFile(
            appContext,
            "${packageName()}.fileprovider",
            File(file.absolutePath())
        )
        installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
        updateStatus(
            status = "下载完成，正在启动安装...",
            patch = false,
            progress = 100,
            downloading = false,
            indeterminate = false,
        )
        appContext.startActivity(installIntent)
        return Result.success()
    }

    override fun whenError(t: Throwable) {
        super.whenError(t)
        val (statusText, notification) = when (t) {
            is DownloadError.MD5CheckFailed -> "MD5 校验失败，请重新下载" to md5Failed()
            else -> {
                Log.w(TAG, "download apk failed", t)
                val msg = t.message?.takeIf { it.isNotBlank() }?.let { "下载失败：$it" } ?: "下载失败，请稍后重试"
                msg to downloadFailed()
            }
        }
        updateStatus(
            status = statusText,
            patch = false,
            progress = 0,
            downloading = false,
            indeterminate = false,
            failed = true,
        )
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

    private suspend fun startDownload(versionName: String) =
        notificationBuilder
            .setContentTitle("正在下载：${versionName}")
            .setContentText("正在开始下载")
            .build()

    private fun getDownloadUrl(versionName: String) =
        notificationBuilder
            .setContentTitle("正在下载：${versionName}")
            .setContentText("正在获取下载地址……")
            .build()

    private fun updateProgress(downloadUpdateState: DownloadUpdateState) =
        notificationBuilder
            .setProgress(100, downloadUpdateState.progress, false)
            .setContentText("${downloadUpdateState.downloaded.formatFileSize()}/${downloadUpdateState.totalSize.formatFileSize()}")
            .setSubText("已下载${downloadUpdateState.progress}%")
            .build()

    private fun md5Checking() = notificationBuilder
        .setProgress(0, 0, false)
        .setContentTitle("MD5校验中……")
        .setContentText(null)
        .setOngoing(false)
        .build()

    private fun md5Failed() = failedNotificationBuilder
        .setContentTitle("MD5校验失败，请重新下载")
        .build()

    private fun downloadFailed() = failedNotificationBuilder
        .setContentTitle("下载失败")
        .build()

    private fun Notification.notify() {
        with(NotificationManagerCompat.from(appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "notify: no permission to post notifications")
                return
            }
            notify(NOTIFICATION_ID, this@notify)
        }
    }
}
