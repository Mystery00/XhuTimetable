package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val sessionToken: String,
)
