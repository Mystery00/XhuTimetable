package vip.mystery0.xhu.timetable.model.request

data class LoginRequest(
    val username: String,
    val password: String,
    val publicKey: String,
)