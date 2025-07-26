package vip.mystery0.xhu.timetable.config.ktor

data class FileDownloadProgressState(
    val received: Long = 1L,
    val total: Long = 1L,
    val progress: Float = 100.0F,
)