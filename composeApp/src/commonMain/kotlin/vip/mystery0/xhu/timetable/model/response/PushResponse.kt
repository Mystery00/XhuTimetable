package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PushResponse(
    val title: String,
    val content: String,
    val operation: Operation,
)

@Serializable
data class Operation(
    val actionType: String,
    val actionParams: List<String>,
)