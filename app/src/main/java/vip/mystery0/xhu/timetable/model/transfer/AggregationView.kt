package vip.mystery0.xhu.timetable.model.transfer

import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import java.time.LocalDate

data class AggregationView(
    val todayViewList: List<TodayCourseView>,
    val weekViewList: List<WeekCourseView>,
    val todayThingList: List<TodayThingView>,
    val loadWarning: String = "",
    val holiday: Holiday?,
    val tomorrowHoliday: Holiday?,
)

data class Holiday(
    val date: LocalDate,
    val isOffDay: Boolean,
    val name: String,
)

fun Holiday?.showOnTitle(debugMode: Boolean): String =
    when {
        this == null -> ""
        !isOffDay -> if (debugMode) "[isOffDay=false]" else ""
        else -> " - $name"
    }