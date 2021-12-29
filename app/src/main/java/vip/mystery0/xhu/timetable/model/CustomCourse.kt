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
) {
    companion object {
        val PLACEHOLDER =
            CustomCourse(0, "课程名称", "教师名称", "第1周", listOf(1), "上课地点", listOf(1, 1), 1, "")
        val EMPTY =
            CustomCourse(0, "", "", "", listOf(1), "", listOf(1, 1), 1, "")
    }
}