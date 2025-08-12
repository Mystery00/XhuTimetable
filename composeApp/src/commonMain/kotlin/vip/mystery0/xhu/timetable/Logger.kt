package vip.mystery0.xhu.timetable

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import vip.mystery0.xhu.timetable.config.logger.FileLogWriter

fun initLogger(): FileLogWriter {
    val fileLogWriter = FileLogWriter()
    Logger.setTag("XhuTimetable")
    var consoleMinSeverity = Severity.Warn
    if (isDebug) consoleMinSeverity = Severity.Verbose
    Logger.setLogWriters(
        SpecificLevelLogWriter(
            consoleMinSeverity,
            platformLogWriter(NoTagFormatter),
        ), fileLogWriter
    )
    return fileLogWriter
}

expect val isDebug: Boolean

class SpecificLevelLogWriter(
    val minSeverity: Severity,
    val logWriter: LogWriter,
) : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity): Boolean = minSeverity <= severity

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) = logWriter.log(severity, message, tag, throwable)
}