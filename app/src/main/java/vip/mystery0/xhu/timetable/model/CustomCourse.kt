package vip.mystery0.xhu.timetable.model

data class CustomCourse(
    var courseId: Long,
    var courseName: String,
    var teacherName: String,
    var weekString: String,
    var week: List<Int>,
    var location: String,
    var courseIndex: List<Int>,
    var day: Int,
    var extraData: String,
)