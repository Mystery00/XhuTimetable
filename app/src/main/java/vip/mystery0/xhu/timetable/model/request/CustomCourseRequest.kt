package vip.mystery0.xhu.timetable.model.request

data class CustomCourseRequest(
    val courseName: String,
    val teacherName: String,
    val week: List<Int>,
    val location: String,
    val startIndex: Int,
    val endIndex: Int,
    val day: Int,
    val extraData: String,
    val year: String,
    val term: Int,
)