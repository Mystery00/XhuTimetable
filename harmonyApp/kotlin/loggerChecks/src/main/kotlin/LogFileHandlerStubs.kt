package vip.mystery0.xhu.timetable.config.logger

import io.github.vinceglb.filekit.PlatformFile

class LogFileHandler {
    fun enable() = false
    fun clearOld() = Unit
    fun writeLogs(logs: List<String>): Unit = error("禁用文件日志时不应执行写入")
    suspend fun prepareSend(): PlatformFile = error("文件日志未启用")
}
