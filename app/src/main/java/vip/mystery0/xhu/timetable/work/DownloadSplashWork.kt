package vip.mystery0.xhu.timetable.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.config.store.getCacheStore
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
        val files = dir.listFiles()!!.toMutableSet()
        val splashList = getCacheStore { splashList }
            .map { splash ->
                val extension = splash.imageUrl.substringAfterLast(".")
                val name = "${splash.splashId.toString().sha1()}-${splash.imageUrl.md5()}"
                val file = File(dir, "${name.sha256()}.${extension}")
                files.removeIf { it.absolutePath == file.absolutePath }
                splash.imageUrl to file
            }
            .filter {
                !it.second.exists()
            }
        files.forEach { it.delete() }
        if (splashList.isEmpty()) {
            return Result.success()
        }
        splashList.forEach {
            val response = fileApi.download(it.first)
            withContext(Dispatchers.IO) {
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
