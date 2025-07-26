package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val publicKey: String,
    val clientPublicKey: String,
    val native: Boolean = true,
)