package vip.mystery0.xhu.timetable.model.response

data class ExperimentScoreResponse(
    //教学班名称
    val teachingClassName: String,
    //课程名称
    val courseName: String,
    //总成绩
    val totalScore: Double,
    //子项列表
    val itemList: List<ExperimentScoreItemResponse>,
) {
    companion object {
        val PLACEHOLDER = ExperimentScoreResponse(
            "教学班名称",
            "课程名称",
            0.0,
            listOf(ExperimentScoreItemResponse.PLACEHOLDER)
        )
    }
}

data class ExperimentScoreItemResponse(
    //实验项目名称
    val experimentProjectName: String,
    //学分
    val credit: Double,
    //成绩
    val score: Double,
    //成绩说明
    val scoreDescription: String,
    //选必做
    val mustTest: String,
) {
    companion object {
        val PLACEHOLDER = ExperimentScoreItemResponse(
            "实验项目名称",
            0.0,
            0.0,
            "成绩说明",
            "选必做"
        )
    }
}
