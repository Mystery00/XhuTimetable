package vip.mystery0.xhu.timetable.model.response

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest

@Immutable
@Serializable
data class SchoolTimetableResponse(
    val courseName: String,
    val showTimeString: String,
    val location: String,
    val teacher: String,
    val customCourseList: List<CustomCourseRequest>
)