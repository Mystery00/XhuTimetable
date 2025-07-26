package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DayOfWeek

data class WidgetWeekItem(
    //标题
    var showTitle: String,
    //步长
    var step: Int,
    //开始节次序号
    val startIndex: Int,
    //周几
    val day: DayOfWeek,
    //当前格子的课程列表
    var course: ArrayList<WeekCourseView>,
    //背景颜色
    var color: Color,
    //文本颜色
    var textColor: Color,
) {
    companion object {
        fun empty(step: Int, startIndex: Int, day: DayOfWeek): WidgetWeekItem =
            WidgetWeekItem(
                "",
                step,
                startIndex,
                day,
                arrayListOf(),
                Color.Unspecified,
                Color.Unspecified,
            )
    }

    fun isEmpty(): Boolean = course.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as WidgetWeekItem

        if (showTitle != other.showTitle) return false
        return course == other.course
    }

    override fun hashCode(): Int {
        var result = showTitle.hashCode()
        result = 31 * result + course.hashCode()
        return result
    }
}
