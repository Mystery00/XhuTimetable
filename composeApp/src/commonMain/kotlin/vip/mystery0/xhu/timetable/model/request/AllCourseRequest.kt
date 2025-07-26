package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable

@Serializable
data class AllCourseRequest(
    val courseName: String?,
    val teacherName: String?,
    val courseIndex: Int?,
    val day: Int?,
)
