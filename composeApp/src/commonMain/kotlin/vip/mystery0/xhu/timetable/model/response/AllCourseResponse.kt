package vip.mystery0.xhu.timetable.model.response

import kotlinx.datetime.DayOfWeek
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Serializable
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
    val createTime: XhuInstant,
    val updateTime: XhuInstant,
)