package vip.mystery0.xhu.timetable.config.logger

import vip.mystery0.xhu.timetable.context
import java.io.File

actual class LogFileHandler {
    private val logFile: File by lazy {
        File(context.externalCacheDir, "XhuTimetable.txt").apply {
            if (!exists()) {
                createNewFile()
            }
        }
    }

    actual fun enable() = true

    actual fun writeLogs(logs: List<String>) {
        try {
            logFile.appendText(logs.joinToString(separator = "\n") + "\n")
        } catch (e: Exception) {
            // Log to logcat if file writing fails
            android.util.Log.e("FileLogWriter", "Error writing logs to file on Android", e)
        }
    }
}