package vip.mystery0.xhu.timetable.model.request

data class CourseRoomRequest(
    val location: String,
    val week: List<Int>,
    val day: List<Int>,
    val time: List<Int>,
)