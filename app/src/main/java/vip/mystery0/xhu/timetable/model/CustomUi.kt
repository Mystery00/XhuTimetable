package vip.mystery0.xhu.timetable.model

open class CustomUi(
    open val weekItemHeight: Int = 72,
    open val weekBackgroundAlpha: Float = 0.8F,
    open val weekItemCorner: Int = 4,
    open val weekTitleTemplate: String = "{courseName}\n@{location}",
    open val weekNotTitleTemplate: String = "[非本周]\n{courseName}\n@{location}",
    open val weekTitleTextSize: Int = 10,
    open val backgroundImageBlur: Int = 0,
) {
    companion object {
        val DEFAULT = CustomUi()
    }

    fun builder(): Builder = Builder(
        weekItemHeight = weekItemHeight,
        weekBackgroundAlpha = weekBackgroundAlpha,
        weekItemCorner = weekItemCorner,
        weekTitleTemplate = weekTitleTemplate,
        weekNotTitleTemplate = weekNotTitleTemplate,
        weekTitleTextSize = weekTitleTextSize,
        backgroundImageBlur = backgroundImageBlur,
    )

    class Builder(
        override var weekItemHeight: Int,
        override var weekBackgroundAlpha: Float,
        override var weekItemCorner: Int,
        override var weekTitleTemplate: String,
        override var weekNotTitleTemplate: String,
        override var weekTitleTextSize: Int,
        override var backgroundImageBlur: Int,
    ) : CustomUi(
        weekItemHeight,
        weekBackgroundAlpha,
        weekItemCorner,
        weekTitleTemplate,
        weekNotTitleTemplate,
        weekTitleTextSize,
        backgroundImageBlur,
    )
}