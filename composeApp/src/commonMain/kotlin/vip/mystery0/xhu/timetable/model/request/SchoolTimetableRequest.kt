package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SchoolTimetableRequest(
    val campusId: String?,
    val collegeId: String?,
    val majorId: String?,
    val courseName: String?,
    val teacherName: String?,
)
