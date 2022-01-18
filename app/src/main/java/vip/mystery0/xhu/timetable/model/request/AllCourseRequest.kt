package vip.mystery0.xhu.timetable.model.request

data class AllCourseRequest(
    val year: String,
    val term: Int,
    val courseName: String?,
    val teacherName: String?,
    val courseIndex: Int?,
    val day: Int?,
)
