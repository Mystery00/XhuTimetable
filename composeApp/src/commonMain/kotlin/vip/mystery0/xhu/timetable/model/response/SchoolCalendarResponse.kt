package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class SchoolCalendarResponse(
    val area: String,
    val imageUrl: String,
)