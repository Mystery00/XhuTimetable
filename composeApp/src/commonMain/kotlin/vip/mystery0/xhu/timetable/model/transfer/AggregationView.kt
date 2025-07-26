package vip.mystery0.xhu.timetable.model.transfer

import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView

data class AggregationView(
    val todayViewList: List<TodayCourseView>,
    val weekViewList: List<WeekCourseView>,
    val todayThingList: List<TodayThingView>,
    val loadWarning: String = "",
)