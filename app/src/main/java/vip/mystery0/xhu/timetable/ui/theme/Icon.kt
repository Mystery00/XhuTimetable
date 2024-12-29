package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
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
    val userCampus: Painter
        @Composable
        get() = painterResource(R.drawable.ic_user_campus)
    val customCourseColor: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_course_color)
    val schoolCalendar: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_school_calendar)
    val exportCalendar: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_export_calendar)
    val customCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_course)
    val customThing: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_thing)
    val customBackground: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_background)
    val customUi: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_custom_ui)
    val disableBackgroundWhenNight: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_disable_when_night)
    val enableCalendarView: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_enable_calendar_view)
    val nightMode: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_night_mode)
    val notifyCourse: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_notify_course)
    val holiday: Painter
        @Composable
        get() = painterResource(id = R.drawable.ic_action_holiday)
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
        get() = painterResource(R.drawable.ic_poems)
    val checked: Painter
        @Composable
        get() = painterResource(R.drawable.ic_checked)
    val reset: Painter
        @Composable
        get() = painterResource(R.drawable.ic_round_settings_backup_restore)
    val close: Painter
        @Composable
        get() = painterResource(R.drawable.ic_action_close)
    val clearSplash: Painter
        @Composable
        get() = painterResource(R.drawable.ic_action_clear_splash)
    val github: Painter
        @Composable
        get() = painterResource(R.drawable.ic_github)
    val cornerSuccess: Painter
        @Composable
        get() = painterResource(R.drawable.ic_corner_success)
    val cornerFailed: Painter
        @Composable
        get() = painterResource(R.drawable.ic_corner_failed)
    val hot: Painter
        @Composable
        get() = painterResource(R.drawable.ic_hot)

    object CustomCourse {
        val title: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_title_night) to
                        painterResource(R.drawable.ic_custom_course_title)
            )
        val teacher: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_teacher_night) to
                        painterResource(R.drawable.ic_custom_course_teacher)
            )
        val week: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_week_night) to
                        painterResource(R.drawable.ic_custom_course_week)
            )
        val time: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_time_night) to
                        painterResource(R.drawable.ic_custom_course_time)
            )
        val location: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_location_night) to
                        painterResource(R.drawable.ic_custom_course_location)
            )
        val close: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_close_night) to
                        painterResource(R.drawable.ic_custom_course_close)
            )
        val remark: Painter
            @Composable
            get() = iconOf(
                painterResource(R.drawable.ic_custom_course_remark_night) to
                        painterResource(R.drawable.ic_custom_course_remark)
            )
    }

    object Profile {
        val exam: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_exam_night) to
                        painterResource(id = R.drawable.ic_exam)
            )
        val score: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_score_night) to
                        painterResource(id = R.drawable.ic_score)
            )
        val classroom: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_classroom_night) to
                        painterResource(id = R.drawable.ic_classroom)
            )
        val accountSettings: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_account_settings_night) to
                        painterResource(id = R.drawable.ic_account_settings)
            )
        val classSettings: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_class_settings_night) to
                        painterResource(id = R.drawable.ic_class_settings)
            )
        val settings: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_settings_night) to
                        painterResource(id = R.drawable.ic_settings)
            )
        val notice: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_notice_night) to
                        painterResource(id = R.drawable.ic_notice)
            )
        val feedback: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_feedback_night) to
                        painterResource(id = R.drawable.ic_feedback)
            )
        val share: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_share_night) to
                        painterResource(id = R.drawable.ic_share)
            )
        val urge: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_urge_night) to
                        painterResource(id = R.drawable.ic_urge)
            )
        val expScore: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_exp_score_night) to
                        painterResource(id = R.drawable.ic_exp_score)
            )
        val serverDetect: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_server_detect_night) to
                        painterResource(id = R.drawable.ic_server_detect)
            )
        val job: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_job_night) to
                        painterResource(id = R.drawable.ic_job)
            )
        val joinGroup: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_join_group_night) to
                        painterResource(id = R.drawable.ic_join_group)
            )
        val unknownMenu: Painter
            @Composable
            get() = iconOf(
                painterResource(id = R.drawable.ic_unknown_menu_night) to
                        painterResource(id = R.drawable.ic_unknown_menu)
            )
    }

    object Action {
        val done: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_done)
        val view: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_view)
        val send: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_send)
        val search: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_action_search)
        val addCircle: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_add_circle)
        val sync: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_sync)
        val switch: Painter
            @Composable
            get() = painterResource(R.drawable.ic_action_switch)
        val weekView: Painter
            @Composable
            get() = painterResource(R.drawable.ic_week_view)
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
        val adminOnline: Painter
            @Composable
            get() = painterResource(id = R.drawable.ic_admin_status)
    }
}

@Composable
fun stateOf(checked: Boolean, pair: Pair<Pair<Int, Int>, Pair<Int, Int>>): Painter {
    val (checkedId, uncheckedId) = if (isDarkMode()) pair.first else pair.second
    return if (checked) painterResource(id = checkedId) else painterResource(id = uncheckedId)
}

object XhuStateIcons {
    val todayCourse =
        (R.drawable.ic_today_course_night to R.drawable.ic_today_course_unchecked_night) to
                (R.drawable.ic_today_course to R.drawable.ic_today_course_unchecked)
    val weekCourse =
        (R.drawable.ic_week_course_night to R.drawable.ic_week_course_unchecked_night) to
                (R.drawable.ic_week_course to R.drawable.ic_week_course_unchecked)
    val calendar =
        (R.drawable.ic_calendar_night to R.drawable.ic_calendar_unchecked_night) to
                (R.drawable.ic_calendar to R.drawable.ic_calendar_unchecked)
    val profile = (R.drawable.ic_profile_night to R.drawable.ic_profile_unchecked_night) to
            (R.drawable.ic_profile to R.drawable.ic_profile_unchecked)
}

object XhuImages {
    val defaultBackgroundImage: Int
        get() = R.mipmap.main_bg
    val defaultProfileImage: Int
        get() = R.mipmap.ic_launcher
}

object ProfileImages {
    private val boyPool = listOf(
        R.drawable.img_boy1,
        R.drawable.img_boy2,
        R.drawable.img_boy3,
        R.drawable.img_boy4,
    )
    private val girlPool = listOf(
        R.drawable.img_girl1,
        R.drawable.img_girl2,
        R.drawable.img_girl3,
        R.drawable.img_girl4,
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