package vip.mystery0.xhu.timetable.config.logger

expect class LogFileHandler() {
    fun enable(): Boolean

    /**
     * Appends a list of formatted log strings to the designated log file.
     * Each platform will implement the specific file I/O logic here.
     */
    fun writeLogs(logs: List<String>)
}