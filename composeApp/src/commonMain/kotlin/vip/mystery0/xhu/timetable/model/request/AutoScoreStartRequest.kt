package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AutoScoreStartRequest(
    val publicKey: String,
    val password: String,
    val registrationId: String,
    val platform: String,
)
