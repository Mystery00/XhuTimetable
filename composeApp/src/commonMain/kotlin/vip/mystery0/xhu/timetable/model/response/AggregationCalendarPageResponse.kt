package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalTime

@Serializable
data class CalendarWeekResponse(
    val weekNum: Int,
    val startDate: XhuLocalDate,
    val endDate: XhuLocalDate,
    val items: List<CalendarDayResponse>,
)

@Serializable
data class CalendarDayResponse(
    val showDate: XhuLocalDate,
    val items: List<CalendarDayItemResponse>,
)

@Serializable
data class CalendarDayItemResponse(
    val title: String,
    val startTime: XhuLocalTime,
    val endTime: XhuLocalTime,
    val location: String,
    val type: CalendarDayItemType,
    val courseName: String = "",
    val customThingColor: String = "",
)

enum class CalendarDayItemType {
    COURSE,
    EXPERIMENT_COURSE,
    CUSTOM_COURSE,
    CUSTOM_THING,
}