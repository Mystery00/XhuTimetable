package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ScoreGpaResponse(
    val totalScore: Double,
    val averageScore: Double,
    val totalCredit: Double,
    val gpa: Double,
)