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
)