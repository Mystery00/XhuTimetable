package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.externalDownloadDir
import vip.mystery0.xhu.timetable.packageName
import vip.mystery0.xhu.timetable.ui.activity.DownloadUpdateState
import vip.mystery0.xhu.timetable.ui.activity.addDownloadObserver
import vip.mystery0.xhu.timetable.ui.activity.formatFileSize
import vip.mystery0.xhu.timetable.ui.activity.removeDownloadObserver
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_DOWNLOAD
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.utils.BsPatch
import vip.mystery0.xhu.timetable.utils.md5
import java.io.File
import java.io.FileOutputStream

class DownloadPatchWork(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadPatchWork"
        private const val NOTIFICATION_TAG = "DownloadNotification"
        private val NOTIFICATION_ID = NotificationId.DOWNLOAD.id
    }

    private val notificationManager: NotificationManager by inject()
    private val serverApi: ServerApi by inject()
    private val fileApi: FileApi by inject()
    private var versionName: String = appVersionName

    override suspend fun doWork(): Result {
        val version = DataHolder.version ?: return Result.success()
        val observerId = addDownloadObserver(patchObserver = false) {
            updateProgress(it)
        }
        val dir = File(externalDownloadDir, "patch")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${version.versionName}-${version.versionCode}.patch")
        if (file.exists()) {
            file.delete()
        }
        startDownload()
        //获取下载地址
        val versionUrl = serverApi.versionUrl(version.versionId)
        val response = fileApi.download(versionUrl.patchUrl)
        runOnIo {
            response.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        removeDownloadObserver(patchObserver = false, observerId)
        Log.i(TAG, "save patch to ${file.absolutePath}")
        //检查md5
        md5Checking()
        val md5 = file.md5()
        if (md5 == versionUrl.apkMd5) {
            //md5校验通过，合并安装包
            val apkDir = File(externalDownloadDir, "apk")
            val apkFile = File(apkDir, "${version.versionName}-${version.versionCode}.apk")
            BsPatch.patch(
                applicationContext.applicationInfo.sourceDir,
                apkFile.absolutePath,
                file.absolutePath,
            )
            val installIntent = Intent(Intent.ACTION_VIEW)
            installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(appContext, packageName, apkFile)
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive")
            appContext.startActivity(installIntent)
        } else {
            md5Failed()
        }
        return Result.success()
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_DOWNLOAD)
            .setSound(null)
            .setVibrate(null)
            .setSmallIcon(R.drawable.ic_file_download)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentTitle("正在下载：${versionName}")
            .setContentText("正在获取下载地址……")

    private fun startDownload() =
        notificationBuilder
            .build()
            .show()

    private fun updateProgress(downloadUpdateState: DownloadUpdateState) =
        notificationBuilder
            .setProgress(100, 0, false)
            .setContentText("${downloadUpdateState.downloaded.formatFileSize()}/${downloadUpdateState.totalSize.formatFileSize()}")
            .setSubText("已下载${downloadUpdateState.progress}%")
            .build()
            .show()

    private fun md5Checking() =
        notificationBuilder
            .setProgress(0, 0, false)
            .setContentTitle("MD5校验中……")
            .setContentText(null)
            .setOngoing(false)
            .build()
            .show()

    private fun md5Failed() =
        notificationBuilder
            .setProgress(0, 0, false)
            .setContentTitle("MD5校验失败，请重新下载")
            .setContentText(null)
            .setOngoing(false)
            .build()
            .show()

    private fun Notification.show() {
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, this)
    }
}
