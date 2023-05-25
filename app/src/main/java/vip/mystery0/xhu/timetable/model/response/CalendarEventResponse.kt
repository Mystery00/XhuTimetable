package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

data class CalendarEventResponse(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val location: String,
    val description: String,
    val allDay: Boolean,
    val attenders: List<String>,
)