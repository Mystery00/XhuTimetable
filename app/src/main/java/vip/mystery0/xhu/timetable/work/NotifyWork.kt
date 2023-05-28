package vip.mystery0.xhu.timetable.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.utils.asInstant
import java.time.LocalDate

class NotifyWork(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotifySetter.lock {
            val action = NotifyAction(appContext)
            runCatching {
                action.checkNotifyCourse()
            }
            runCatching {
                action.checkNotifyExam()
            }
        }
        return complete()
    }

    private suspend fun complete(): Result {
        getConfigStore { notifyTime }?.let {
            NotifySetter.setWorkTrigger(it.atDate(LocalDate.now().plusDays(1)).asInstant())
        }
        return Result.success()
    }
}
