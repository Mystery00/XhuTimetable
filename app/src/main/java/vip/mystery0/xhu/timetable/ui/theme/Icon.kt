package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.utils.md5

@Composable
private fun iconOf(pair: Pair<Painter, Painter>): Painter =
    if (isDarkMode()) pair.first else pair.second

object XhuIcons {
    val todayWaterMelon: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_today_course_watermelon)
    val conflict: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_radius_cell)
    val back: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_back)
    val add: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_add)
    val multiUser: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_multi_user)
    val showNotThisWeek: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_show_not_this_week)
    val showCourseStatus: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_show_status)
    val customYearTerm: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_year_term)
    val customStartTime: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_start_time)
    val customCourseColor: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_course_color)
    val schoolCalendar: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_school_calendar)
    val customCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_course)
    val customThing: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_thing)
    val nightMode: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_night_mode)
    val pageEffect: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_page_effect)
    val notifyCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_notify_course)
    val notifyExam: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_notify_exam)
    val notifyTime: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_notify_time)
    val checkUpdate: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_check_update)
    val allowUploadCrash: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_allow_upload_crash)
    val qqGroup: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_qq_group)
    val poems: Painter
        @Composable
        get() = painterResource(R.mipmap.ic_poems)

    object Profile {
        val exam: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_exam_night) to painterResource(
                    id = R.drawable.ic_exam
                )
            )
        val score: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_score_night) to painterResource(
                    id = R.drawable.ic_score
                )
            )
        val classroom: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_classroom_night) to painterResource(
                    id = R.drawable.ic_classroom
                )
            )
        val accountSettings: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_account_settings_night) to painterResource(
                    id = R.drawable.ic_account_settings
                )
            )
        val classSettings: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_class_settings_night) to painterResource(
                    id = R.drawable.ic_class_settings
                )
            )
        val settings: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_settings_night) to painterResource(
                    id = R.drawable.ic_settings
                )
            )
        val notice: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_notice_night) to painterResource(
                    id = R.drawable.ic_notice
                )
            )
        val feedback: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_feedback_night) to painterResource(
                    id = R.drawable.ic_feedback
                )
            )
        val share: Painter
            @Composable
            get() = iconOf(
                pair = painterResource(id = R.drawable.ic_share_night) to painterResource(
                    id = R.drawable.ic_share
                )
            )
    }

    object Action {
        val manage: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_manage)
        val done: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_done)
        val more: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_more)
        val view: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_view)
    }

    object WsState {
        val connected: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_ws_state_connected)
        val connecting: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_ws_state_connecting)
        val disconnected: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_ws_state_disconnected)
        val failed: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_ws_state_failed)
    }

    object Team {
        val yue: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_yue)
        val pan: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_pan)
        val johnny: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_johnny)
        val quinn: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_quinn)
        val chen: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_chen)
        val mystery0: Painter
            @Composable
            get() = painterResource(id = R.mipmap.img_mystery0)
    }
}

@Composable
fun stateOf(checked: Boolean, pair: Pair<Pair<Int, Int>, Pair<Int, Int>>): Painter {
    val icon = if (isDarkMode()) pair.first else pair.second
    return if (checked) painterResource(id = icon.first) else painterResource(id = icon.second)
}

object XhuStateIcons {
    val todayCourse =
        (R.drawable.ic_today_course_night to R.drawable.ic_today_course_unchecked_night) to
                (R.drawable.ic_today_course to R.drawable.ic_today_course_unchecked)
    val weekCourse =
        (R.drawable.ic_week_course_night to R.drawable.ic_week_course_unchecked_night) to
                (R.drawable.ic_week_course to R.drawable.ic_week_course_unchecked)
    val profile = (R.drawable.ic_profile_night to R.drawable.ic_profile_unchecked_night) to
            (R.drawable.ic_profile to R.drawable.ic_profile_unchecked)
}

object XhuImages {
    val defaultBackgroundImage: Int
        get() = R.mipmap.main_bg
    val defaultProfileImage: Int
        get() = R.mipmap.ic_launcher
    val noData: Painter
        @Composable
        get() = rememberImagePainter(R.drawable.ic_no_data)
    val noCourse: Painter
        @Composable
        get() = rememberImagePainter(R.drawable.ic_no_course)
}

object ProfileImages {
    private val boyPool = listOf(
        R.drawable.ic_boy_01,
        R.drawable.ic_boy_02,
        R.drawable.ic_boy_03,
        R.drawable.ic_boy_04,
        R.drawable.ic_boy_05,
    )
    private val girlPool = listOf(
        R.drawable.ic_girl_01,
        R.drawable.ic_girl_02,
        R.drawable.ic_girl_03,
        R.drawable.ic_girl_04,
        R.drawable.ic_girl_05,
    )

    @Composable
    fun hash(
        userName: String,
        boy: Boolean
    ): Painter {
        val md5Int = userName.md5().substring(0, 2).toInt(16)
        val pool = if (boy) boyPool else girlPool
        return painterResource(pool[md5Int % pool.size])
    }
}