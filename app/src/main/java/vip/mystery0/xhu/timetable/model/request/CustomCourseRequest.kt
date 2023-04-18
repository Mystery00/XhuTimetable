package vip.mystery0.xhu.timetable.model.request

import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import java.time.DayOfWeek

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
                day.value,
                startDayTime,
                endDayTime,
                location,
                teacher,
                emptyList(),
                0,
                0,
            )

        fun buildOf(
            allCourseResponse: AllCourseResponse,
        ): CustomCourseRequest =
            CustomCourseRequest(
                allCourseResponse.courseName,
                allCourseResponse.weekList,
                allCourseResponse.day.value,
                allCourseResponse.startDayTime,
                allCourseResponse.endDayTime,
                allCourseResponse.location,
                allCourseResponse.teacher,
                emptyList(),
                0,
                0,
            )
    }
}