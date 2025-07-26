package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class TeamMemberResponse(
    val title: String,
    val subtitle: String,
    val icon: String,
)