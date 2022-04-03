package vip.mystery0.xhu.timetable.model

open class CustomUi(
    open val weekItemHeight: Int,
    open val weekBackgroundAlpha: Float,
    open val weekItemCorner: Int,
    open val weekTitleTemplate: String,
    open val weekNotTitleTemplate: String,
    open val weekTitleTextSize: Int,
) {
    companion object {
        val DEFAULT = CustomUi(
            weekItemHeight = 72,
            weekBackgroundAlpha = 0.8F,
            weekItemCorner = 4,
            weekTitleTemplate = "{courseName}\n@{location}",
            weekNotTitleTemplate = "[非本周]\n{courseName}\n@{location}",
            weekTitleTextSize = 10,
        )
    }

    fun builder(): Builder = Builder(
        weekItemHeight = weekItemHeight,
        weekBackgroundAlpha = weekBackgroundAlpha,
        weekItemCorner = weekItemCorner,
        weekTitleTemplate = weekTitleTemplate,
        weekNotTitleTemplate = weekNotTitleTemplate,
        weekTitleTextSize = weekTitleTextSize,
    )

    class Builder(
        override var weekItemHeight: Int,
        override var weekBackgroundAlpha: Float,
        override var weekItemCorner: Int,
        override var weekTitleTemplate: String,
        override var weekNotTitleTemplate: String,
        override var weekTitleTextSize: Int,
    ) : CustomUi(
        weekItemHeight,
        weekBackgroundAlpha,
        weekItemCorner,
        weekTitleTemplate,
        weekNotTitleTemplate,
        weekTitleTextSize
    )
}