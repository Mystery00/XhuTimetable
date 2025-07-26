package vip.mystery0.xhu.timetable.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.now

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
            NotifySetter.setWorkTrigger(
                LocalDate.now()
                    .plus(1, DateTimeUnit.DAY)
                    .atTime(it)
                    .asInstant()
            )
        }
        return Result.success()
    }
}
