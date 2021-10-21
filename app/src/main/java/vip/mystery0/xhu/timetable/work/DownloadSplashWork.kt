package vip.mystery0.xhu.timetable.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import java.io.File
import java.io.FileOutputStream

class DownloadSplashWork(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadSplashWork"
    }

    private val fileApi: FileApi by inject()

    override suspend fun doWork(): Result {
        val dir = File(externalPictureDir, "splash")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val splashList = getConfig { splashList }
            .map {
                val extension = it.imageUrl.substringAfterLast(".")
                val name = "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}"
                it.imageUrl to File(
                    dir,
                    "${name.sha256()}.${extension}"
                )
            }
            .filter {
                !it.second.exists()
            }
        if (splashList.isEmpty()) {
            return Result.success()
        }
        splashList.forEach {
            val response = fileApi.download(it.first)
            runOnIo {
                response.byteStream().use { input ->
                    FileOutputStream(it.second).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Log.i(TAG, "save splash to ${it.second.absolutePath}")
        }
        return Result.success()
    }
}
