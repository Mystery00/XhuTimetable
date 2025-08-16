package vip.mystery0.xhu.timetable.config.logger

import io.github.vinceglb.filekit.PlatformFile

actual class LogFileHandler {
    actual fun enable() = false

    actual fun writeLogs(logs: List<String>) {
    }

    actual fun clearOld() {
    }

    actual suspend fun prepareSend(): PlatformFile = throw NotImplementedError()
}