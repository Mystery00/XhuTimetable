package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import vip.mystery0.xhu.timetable.R

object XhuIcons {
    val todayWaterMelon: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_today_course_watermelon)
    val conflict: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_radius_cell)
    val sync: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_sync)

    object Profile {
        val exam: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_exam)
        val score: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_score)
        val classroom: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_classroom)
        val accountSettings: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_account_settings)
        val classSettings: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_class_settings)
        val settings: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_settings)
        val notice: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_notice)
        val share: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_share)
    }
}

@Composable
fun stateOf(checked: Boolean, icon: Pair<Int, Int>): Painter =
    if (checked) painterResource(id = icon.first) else painterResource(id = icon.second)

object XhuStateIcons {
    val todayCourse = R.drawable.ic_today_course to R.drawable.ic_today_course_unchecked
    val weekCourse = R.drawable.ic_week_course to R.drawable.ic_week_course_unchecked
    val profile = R.drawable.ic_profile to R.drawable.ic_profile_unchecked
}