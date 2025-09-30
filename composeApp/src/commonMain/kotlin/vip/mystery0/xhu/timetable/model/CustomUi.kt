package vip.mystery0.xhu.timetable.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomUi(
    val todayBackgroundAlpha: Float = 1F,
    val weekItemHeight: Float = 72F,
    val weekBackgroundAlpha: Float = 0.8F,
    val weekItemCorner: Float = 4F,
    val weekTitleTemplate: String = "{courseName}\n@{location}",
    val weekNotTitleTemplate: String = "[非本周]\n{courseName}\n@{location}",
    val weekTitleTextSize: Float = 10F,
    val backgroundImageBlur: Float = 0F,
) {
    companion object {
        val DEFAULT = CustomUi()
    }
}