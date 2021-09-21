package vip.mystery0.xhu.timetable.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.externalPictureDir
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256
import java.io.File
import java.io.FileOutputStream

class DownloadSplashWork(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    private val fileApi: FileApi by inject()

    override suspend fun doWork(): Result {
        val dir = externalPictureDir
        val splashList = Config.splashList
            .map {
                it.imageUrl to File(
                    dir,
                    "${it.splashId.toString().sha1()}-${it.imageUrl.md5()}".sha256()
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
            withContext(Dispatchers.IO) {
                response.byteStream().use { input ->
                    FileOutputStream(it.second).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
        return Result.success()
    }
}
