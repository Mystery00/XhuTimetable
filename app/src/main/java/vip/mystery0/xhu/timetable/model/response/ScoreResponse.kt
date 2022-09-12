package vip.mystery0.xhu.timetable.model.response

data class ScoreResponse(
    val list: List<ScoreItem>,
    val failedList: List<ScoreItem>,
)

data class ScoreItem(
    val courseName: String,
    val score: String,
    val gpa: String,
    val credit: String,
    val courseType: String,
    val examType: String,
) {
    companion object {
        val PLACEHOLDER = ScoreItem("课程名称", "成绩", "绩点", "学分", "课程类型", "补考1")
    }
}