package vip.mystery0.xhu.timetable.model.response

data class ExpScoreResponse(
    val courseName: String,
    val expName: String,
    val score: String,
    val credit: String,
    val expType: String,
) {
    companion object {
        val PLACEHOLDER = ExpScoreResponse("课程名称", "实验名称", "成绩", "学分", "课程类型")
    }
}