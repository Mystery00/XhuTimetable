package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import vip.mystery0.xhu.timetable.ui.screen.calendarActions
import vip.mystery0.xhu.timetable.ui.screen.calendarContent
import vip.mystery0.xhu.timetable.ui.screen.calendarTitleBar
import vip.mystery0.xhu.timetable.ui.screen.profileCourseContent
import vip.mystery0.xhu.timetable.ui.screen.profileCourseTitleBar
import vip.mystery0.xhu.timetable.ui.screen.todayCourseActions
import vip.mystery0.xhu.timetable.ui.screen.todayCourseContent
import vip.mystery0.xhu.timetable.ui.screen.todayCourseTitleBar
import vip.mystery0.xhu.timetable.ui.screen.weekCourseActions
import vip.mystery0.xhu.timetable.ui.screen.weekCourseContent
import vip.mystery0.xhu.timetable.ui.screen.weekCourseTitleBar
import vip.mystery0.xhu.timetable.ui.theme.XhuStateIcons

internal enum class Tab(
    val label: String,
    val otherLabel: String = label,
    val icon: Pair<Pair<DrawableResource, DrawableResource>, Pair<DrawableResource, DrawableResource>>,
    val titleBar: TabTitle? = null,
    val actions: TabAction? = null,
    val content: TabContent,
) {
    TODAY(
        label = "今日",
        otherLabel = "明日",
        icon = XhuStateIcons.todayCourse,
        titleBar = todayCourseTitleBar,
        actions = todayCourseActions,
        content = todayCourseContent,
    ),
    WEEK(
        label = "本周",
        icon = XhuStateIcons.weekCourse,
        titleBar = weekCourseTitleBar,
        actions = weekCourseActions,
        content = weekCourseContent,
    ),
    CALENDAR(
        label = "月历",
        icon = XhuStateIcons.calendar,
        titleBar = calendarTitleBar,
        actions = calendarActions,
        content = calendarContent,
    ),
    PROFILE(
        label = "我的",
        icon = XhuStateIcons.profile,
        titleBar = profileCourseTitleBar,
        content = profileCourseContent,
    ),
}

internal fun tabOfWhenEnableCalendar(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.CALENDAR
    3 -> Tab.PROFILE
    else -> Tab.PROFILE
}

internal fun tabOfWhenDisableCalendar(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.PROFILE
    else -> Tab.PROFILE
}

typealias TabTitle = @Composable () -> Unit
typealias TabAction = @Composable RowScope.() -> Boolean
typealias TabContent = @Composable ColumnScope.() -> Unit