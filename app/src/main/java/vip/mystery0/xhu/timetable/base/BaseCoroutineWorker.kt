package vip.mystery0.xhu.timetable.base

import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ForegroundInfo
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.await
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseCoroutineWorker(
    appContext: Context,
    params: WorkerParameters
) : ListenableWorker(appContext, params) {
    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer ->
            val coroutineScope = CoroutineScope(Dispatchers.Default)
            coroutineScope.launch {
                try {
                    val result = doWork()
                    completer.set(result)
                } catch (t: Throwable) {
                    whenError(t)
                    completer.setException(t)
                }
            }
        }
    }

    open suspend fun whenError(t: Throwable) {
    }

    abstract suspend fun doWork(): Result

    suspend fun setForeground(foregroundInfo: ForegroundInfo) {
        setForegroundAsync(foregroundInfo).await()
    }
}

sealed class CoroutineWorkerException(override val message: String?) : RuntimeException(message)

object DownloadError {
    class MD5CheckFailed : CoroutineWorkerException("md5 check failed")
    class PatchFailed : CoroutineWorkerException("apk patch failed")
}
