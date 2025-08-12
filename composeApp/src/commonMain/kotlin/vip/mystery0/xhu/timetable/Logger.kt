package vip.mystery0.xhu.timetable

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

fun initLogger() {
    Logger.setLogWriters(platformLogWriter(NoTagFormatter))
    Logger.setTag("XhuTimetable")
    if (!isDebug) {
        Logger.setMinSeverity(Severity.Warn)
    }
}

expect val isDebug: Boolean