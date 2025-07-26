package vip.mystery0.xhu.timetable.viewmodel

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.java.KoinJavaComponent.inject
import vip.mystery0.xhu.timetable.work.DownloadSplashWork
import vip.mystery0.xhu.timetable.work.NotifySetter

actual suspend fun doPlatformInit() {
    NotifySetter.setTrigger()
}

actual suspend fun doCheckDownloadSplash() {
    val workManager by inject<WorkManager>(WorkManager::class.java)
    workManager.enqueue(
        OneTimeWorkRequestBuilder<DownloadSplashWork>()
            .build()
    )
}