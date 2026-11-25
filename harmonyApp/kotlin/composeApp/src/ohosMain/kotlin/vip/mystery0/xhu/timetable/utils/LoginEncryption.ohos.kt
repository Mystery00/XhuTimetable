package vip.mystery0.xhu.timetable.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import vip.mystery0.xhu.timetable.platform.ArkTsBridge

actual suspend fun encryptLoginPassword(
    password: String,
    serverPublicKey: String,
    nonce: String,
): LoginEncryptionResult {
    val request = buildJsonObject {
        put("password", password)
        put("serverPublicKey", serverPublicKey)
        put("nonce", nonce)
    }
    val response = Json.parseToJsonElement(
        ArkTsBridge.call("encryptLoginPassword", request.toString())
    ).jsonObject
    return LoginEncryptionResult(
        response.getValue("encryptedPassword").jsonPrimitive.content,
        response.getValue("clientPublicKey").jsonPrimitive.content,
    )
}
