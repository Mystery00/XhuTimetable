package vip.mystery0.xhu.timetable.nativeui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

val nativeJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun JsonObject.text(key: String, default: String = ""): String =
    (get(key) as? JsonPrimitive)?.contentOrNull ?: default

fun JsonObject.number(key: String, default: Int = 0): Int =
    (get(key) as? JsonPrimitive)?.intOrNull ?: default

fun JsonObject.flag(key: String, default: Boolean = false): Boolean =
    (get(key) as? JsonPrimitive)?.booleanOrNull ?: default

fun JsonObject.numbers(key: String): List<Int> =
    (get(key) as? JsonArray)?.map { it.jsonPrimitive.int } ?: emptyList()

fun Color.nativeHex(): String = "#" + toArgb().toUInt().toString(16).padStart(8, '0')

fun option(value: String, label: String): JsonObject = buildJsonObject {
    put("value", value)
    put("label", label)
}

fun displayItem(
    id: String = "", title: String, subtitle: String = "", detail: String = "",
    time: String = "", location: String = "", color: String = "#526F5E",
    status: String = "", badge: String = "", value: String = "", image: String = "", url: String = "",
    actions: JsonArray = JsonArray(emptyList()),
    startTime: String = "", endTime: String = "", date: String = "",
    fields: List<JsonObject> = emptyList(), children: List<JsonObject> = emptyList(), selected: Boolean = false,
): JsonObject = buildJsonObject {
    put("id", id)
    put("title", title)
    put("subtitle", subtitle)
    put("detail", detail)
    put("time", time)
    put("location", location)
    put("color", color)
    put("status", status)
    put("badge", badge)
    put("value", value)
    put("image", image)
    put("url", url)
    put("actions", actions)
    put("startTime", startTime)
    put("endTime", endTime)
    put("date", date)
    put("fields", JsonArray(fields))
    put("children", JsonArray(children))
    put("selected", selected)
}

fun detailField(key: String, label: String, value: String): JsonObject = buildJsonObject {
    put("key", key)
    put("label", label)
    put("value", value)
}

fun listResult(
    title: String, items: List<JsonObject>, page: Int = 1,
    hasMore: Boolean = false, summary: String = "", metrics: List<JsonObject> = emptyList(),
): JsonObject = buildJsonObject {
    put("title", title)
    put("summary", summary)
    put("items", JsonArray(items))
    put("page", page)
    put("hasMore", hasMore)
    put("metrics", JsonArray(metrics))
}

suspend fun userSummary(user: User, mainId: String?): JsonObject = buildJsonObject {
    put("id", user.studentId)
    put("name", user.info.name)
    put("title", getConfigStore { customAccountTitle }.formatToday(user.info))
    put("college", user.info.college)
    put("major", user.info.majorName)
    put("className", user.info.className)
    put("gender", user.info.gender.showTitle)
    put("grade", user.info.xhuGrade)
    put("direction", user.info.majorDirection)
    put("campus", "")
    put("main", user.studentId == mainId)
    put("color", ColorPool.hash(user.studentId).nativeHex())
}

suspend fun userSummaries(): JsonArray {
    val mainId = UserStore.getMainUser()?.studentId
    return JsonArray(UserStore.loggedUserList().map { userSummary(it, mainId) })
}
