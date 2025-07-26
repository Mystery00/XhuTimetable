package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AutoCheckScoreRequest(
    val username: String,
    val password: String,
    val publicKey: String,
    val registrationId: String,
    val year: Int,
    val term: Int,
)
