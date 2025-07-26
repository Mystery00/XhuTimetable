package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BackgroundResponse(
    val backgroundId: Long,
    val resourceId: Long,
    val thumbnailUrl: String,
    val imageUrl: String,
)

@Serializable
data class ResourceUrlResponse(
    val url: String,
)