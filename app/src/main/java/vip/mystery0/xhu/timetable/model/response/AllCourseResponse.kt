package vip.mystery0.xhu.timetable.model.response

import java.time.DayOfWeek
import java.time.Instant

data class AllCourseResponse(
    val courseName: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val year: Int,
    val term: Int,
    val createTime: Instant,
    val updateTime: Instant,
)