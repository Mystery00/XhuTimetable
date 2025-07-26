package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyResponse(
    val publicKey: String,
    val nonce: String,
)