package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest

@Serializable
data class SchoolTimetableResponse(
    val courseName: String,
    val showTimeString: String,
    val location: String,
    val teacher: String,
    val customCourseList: List<CustomCourseRequest>
)