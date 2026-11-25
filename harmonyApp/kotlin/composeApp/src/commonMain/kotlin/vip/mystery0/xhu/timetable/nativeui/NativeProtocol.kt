package vip.mystery0.xhu.timetable.nativeui

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class NativeRequest(val id: String, val action: String, val payload: JsonObject) {
    companion object {
        fun parse(value: String): NativeRequest {
            require(value.length <= 1_048_576) { "请求内容过长" }
            val root = try {
                Json.parseToJsonElement(value) as? JsonObject
            } catch (_: IllegalArgumentException) {
                null
            } ?: throw IllegalArgumentException("请求格式无效")
            val id = (root["id"] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull.orEmpty()
            val action = (root["action"] as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull.orEmpty()
            require(id.isNotBlank() && id.length <= 128) { "请求标识无效" }
            require(action.isNotBlank() && action.length <= 64) { "操作名称无效" }
            val payload = if ("payload" !in root) JsonObject(emptyMap())
                else root["payload"] as? JsonObject ?: throw IllegalArgumentException("请求参数必须是对象")
            return NativeRequest(id, action, payload)
        }
    }
}
