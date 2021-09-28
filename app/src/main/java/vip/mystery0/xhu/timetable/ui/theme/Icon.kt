package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import vip.mystery0.xhu.timetable.R

object XhuIcons {
    val todayCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_today_course)
    val weekCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_week_course)
    val profile: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_profile)
    val todayWaterMelon: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_today_course_watermelon)
}

@Composable
fun stateOf(checked: Boolean, icon: Pair<Int, Int>): Painter =
    if (checked) painterResource(id = icon.first) else painterResource(id = icon.second)

object XhuStateIcons {
    val todayCourse = R.drawable.ic_today_course to R.drawable.ic_today_course_unchecked
    val weekCourse = R.drawable.ic_week_course to R.drawable.ic_week_course_unchecked
    val profile = R.drawable.ic_profile to R.drawable.ic_profile_unchecked
}