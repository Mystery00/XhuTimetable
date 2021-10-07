package vip.mystery0.xhu.timetable.model.response

data class ExamResponse(
    val list: List<ExamItem>,
    val html: String,
)

data class ExamItem(
    val date: String,
    val examNumber: String,
    val courseName: String,
    val type: String,
    val location: String,
    val startTime: Long,
    val endTime: Long,
    val region: String,
)