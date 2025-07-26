package vip.mystery0.xhu.timetable.base

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.impl.utils.futures.AbstractFuture
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
abstract class XhuCoroutineWorker(
    appContext: Context,
    params: WorkerParameters
) : ListenableWorker(appContext, params) {
    private val job = Job()
    private val future: SettableFuture<Result> = SettableFuture()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        future.addListener(
            {
                if (future.isCancelled) {
                    job.cancel()
                }
            },
            taskExecutor.serialTaskExecutor
        )
    }

    final override fun startWork(): ListenableFuture<Result> {
        coroutineScope.launch {
            try {
                val result = doWork()
                future.set(result)
            } catch (t: Throwable) {
                whenError(t)
                future.setException(t)
            }
        }
        return future
    }

    open fun whenError(t: Throwable) {
    }

    abstract suspend fun doWork(): Result

    override fun onStopped() {
        super.onStopped()
        future.cancel(false)
    }
}

@SuppressLint("RestrictedApi")
class SettableFuture<V> internal constructor() : AbstractFuture<V>() {
    public override fun set(value: V?): Boolean {
        return super.set(value)
    }

    public override fun setException(throwable: Throwable): Boolean {
        return super.setException(throwable)
    }

    public override fun setFuture(future: ListenableFuture<out V>): Boolean {
        return super.setFuture(future)
    }
}

inline fun <reified W : XhuCoroutineWorker> WorkManager.startUniqueWork(inputData: Data) {
    enqueueUniqueWork(
        W::class.java.name,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<W>()
            .setInputData(inputData)
            .build()
    )
}

sealed class CoroutineWorkerException(override val message: String?) : RuntimeException(message)

object DownloadError {
    class MD5CheckFailed : CoroutineWorkerException("md5 check failed")
    class PatchFailed : CoroutineWorkerException("apk patch failed")
}