package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SetCampusRequest(
    val campus: String,
)
