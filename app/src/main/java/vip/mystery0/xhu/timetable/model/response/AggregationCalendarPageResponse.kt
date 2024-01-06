package vip.mystery0.xhu.timetable.model.response

import java.time.LocalDate
import java.time.LocalTime

data class CalendarWeekResponse(
    val weekNum: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val items: List<CalendarDayResponse>,
)

data class CalendarDayResponse(
    val showDate: LocalDate,
    val items: List<CalendarDayItemResponse>,
)

data class CalendarDayItemResponse(
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String,
    val type: CalendarDayItemType,
    val courseName: String = "",
    val seatNo: String = "",
    val customThingColor: String = "",
)

enum class CalendarDayItemType {
    COURSE,
    EXPERIMENT_COURSE,
    CUSTOM_COURSE,
    CUSTOM_THING,
}