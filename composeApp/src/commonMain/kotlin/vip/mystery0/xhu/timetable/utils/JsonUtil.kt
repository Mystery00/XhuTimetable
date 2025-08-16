package vip.mystery0.xhu.timetable.utils

import kotlinx.serialization.json.Json

val mapJson = Json {
    isLenient = true
}
val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

inline fun <reified K, reified V> Map<K, V>.formatMapToJson(): String = mapJson.encodeToString(this)
inline fun <reified K, reified V> String.parseJsonToMap(): Map<K, V> =
    mapJson.decodeFromString(this)

inline fun <reified T> String.parseJsonToObj(): T = json.decodeFromString(this)
inline fun <reified T> T.formatObjToJson(): String = json.encodeToString(this)