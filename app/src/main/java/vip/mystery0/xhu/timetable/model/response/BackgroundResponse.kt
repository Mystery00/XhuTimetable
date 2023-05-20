package vip.mystery0.xhu.timetable.model.response

data class BackgroundResponse(
    val backgroundId: Long,
    val resourceId: Long,
    val thumbnailUrl: String,
    val imageUrl: String,
)

data class ResourceUrlResponse(
    val url: String,
)