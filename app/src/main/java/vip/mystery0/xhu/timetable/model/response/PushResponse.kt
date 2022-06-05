package vip.mystery0.xhu.timetable.model.response

data class PushResponse(
    val title: String,
    val content: String,
    val operation: Operation,
)

data class Operation(
    val actionType: String,
    val actionParams: List<String>,
)