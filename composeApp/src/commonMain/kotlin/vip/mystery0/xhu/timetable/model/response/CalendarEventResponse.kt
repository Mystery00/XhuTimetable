package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Serializable
data class CalendarEventResponse(
    val title: String,
    val startTime: XhuInstant,
    val endTime: XhuInstant,
    val location: String,
    val description: String,
    val allDay: Boolean,
    val attenders: List<String>,
)