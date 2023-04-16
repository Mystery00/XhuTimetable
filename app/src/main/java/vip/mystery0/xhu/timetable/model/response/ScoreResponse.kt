package vip.mystery0.xhu.timetable.model.response

data class ScoreResponse(
    //教学班名称
    val teachingClassName: String,
    //课程编号
    val courseNo: String,
    //课程名称
    val courseName: String,
    //课程性质
    val courseType: String,
    //学分
    val credit: Double,
    //成绩
    val score: Double,
    //成绩说明
    val scoreDescription: String,
    //绩点
    val gpa: Double,
    //成绩性质
    val scoreType: String,
    //学分绩点
    val creditGpa: Double,
) {
    companion object {
        val PLACEHOLDER = ScoreResponse(
            "教学班名称",
            "课程编号",
            "课程名称",
            "课程性质",
            0.0,
            0.0,
            "成绩说明",
            0.0,
            "成绩性质",
            0.0,
        )
    }
}
