package vip.mystery0.xhu.timetable

import co.touchlab.kermit.Logger
import co.touchlab.kermit.koin.KermitKoinLogger
import org.koin.core.context.startKoin
import vip.mystery0.xhu.timetable.module.moduleList

fun callAppInit() {
    initLogger()
    startKoin {
        logger(KermitKoinLogger(Logger.withTag("koin")))
        modules(moduleList())
    }
}