package vip.mystery0.xhu.timetable.utils

data class LoginEncryptionResult(
    val encryptedPassword: String,
    val clientPublicKey: String,
)

expect suspend fun encryptLoginPassword(
    password: String,
    serverPublicKey: String,
    nonce: String,
): LoginEncryptionResult
