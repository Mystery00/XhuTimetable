package vip.mystery0.xhu.timetable.model.request

data class ClassroomRequest(
    val location: String,
    val weekList: List<Int>,
    val dayList: List<Int>,
    val timeList: List<Int>,
)