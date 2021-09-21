package vip.mystery0.xhu.timetable.module

import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.work.DownloadSplashWork

val workModule = module {
    worker { params -> DownloadSplashWork(get(), params.get()) }
}