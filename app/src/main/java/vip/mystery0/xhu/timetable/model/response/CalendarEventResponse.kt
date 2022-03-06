package vip.mystery0.xhu.timetable.model.response

data class CalendarEventResponse(
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String,
    val description: String,
    val allDay: Boolean,
    val attenders: List<String>,
)