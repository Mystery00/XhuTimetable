package vip.mystery0.xhu.timetable.work

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.android.ext.android.inject

class NotifyService : Service() {
    companion object {
        private const val TAG = "NotifyService"
    }

    private val workManager: WorkManager by inject()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate: ")
        workManager.enqueue(
            OneTimeWorkRequestBuilder<NotifyWork>()
                .build()
        )
    }

    override fun onBind(intent: Intent): IBinder? = null
}