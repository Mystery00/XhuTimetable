package vip.mystery0.xhu.timetable.model.request

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.Serializable

@Serializable
data class CustomCourseRequest(
    val courseName: String,
    val weekList: List<Int>,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val extraData: List<String>?,
    var year: Int,
    var term: Int,
) {
    companion object {
        fun buildOf(
            courseName: String,
            weekList: List<Int>,
            day: DayOfWeek,
            startDayTime: Int,
            endDayTime: Int,
            location: String,
            teacher: String,
        ): CustomCourseRequest =
            CustomCourseRequest(
                courseName,
                weekList,
                day.isoDayNumber,
                startDayTime,
                endDayTime,
                location,
                teacher,
                emptyList(),
                0,
                0,
            )
    }
}