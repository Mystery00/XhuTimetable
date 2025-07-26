package vip.mystery0.xhu.timetable.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import vip.mystery0.xhu.timetable.config.externalPictureDir
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.module.HTTP_CLIENT_FILE
import vip.mystery0.xhu.timetable.module.downloadFileTo
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha1
import vip.mystery0.xhu.timetable.utils.sha256

class DownloadSplashWork(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "DownloadSplashWork"
    }

    private val fileClient: HttpClient by inject(named(HTTP_CLIENT_FILE))

    override suspend fun doWork(): Result {
        val dir = PlatformFile(externalPictureDir, "splash")
        if (!dir.exists()) {
            dir.createDirectories(true)
        }
        val files = dir.list().toMutableSet()
        val splashList = getCacheStore { splashList }
            .map { splash ->
                val extension = splash.imageUrl.substringAfterLast(".")
                val name = "${splash.splashId.toString().sha1()}-${splash.imageUrl.md5()}"
                val file = PlatformFile(dir, "${name.sha256()}.${extension}")
                files.removeIf { it.absolutePath() == file.absolutePath() }
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
            fileClient.downloadFileTo(
                saveFile = it.second,
                builder = {
                    url(it.first)
                },
            )
            Log.i(TAG, "save splash to ${it.second.absolutePath()}")
        }
        return Result.success()
    }
}
