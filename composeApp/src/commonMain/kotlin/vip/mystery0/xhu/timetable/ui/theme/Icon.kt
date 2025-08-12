package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vip.mystery0.xhu.timetable.utils.md5
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_account_settings
import xhutimetable.composeapp.generated.resources.ic_account_settings_night
import xhutimetable.composeapp.generated.resources.ic_action_allow_upload_crash
import xhutimetable.composeapp.generated.resources.ic_action_background
import xhutimetable.composeapp.generated.resources.ic_action_check_update
import xhutimetable.composeapp.generated.resources.ic_action_clear_splash
import xhutimetable.composeapp.generated.resources.ic_action_close
import xhutimetable.composeapp.generated.resources.ic_action_custom_course
import xhutimetable.composeapp.generated.resources.ic_action_custom_course_color
import xhutimetable.composeapp.generated.resources.ic_action_custom_start_time
import xhutimetable.composeapp.generated.resources.ic_action_custom_thing
import xhutimetable.composeapp.generated.resources.ic_action_custom_ui
import xhutimetable.composeapp.generated.resources.ic_action_custom_year_term
import xhutimetable.composeapp.generated.resources.ic_action_disable_when_night
import xhutimetable.composeapp.generated.resources.ic_action_done
import xhutimetable.composeapp.generated.resources.ic_action_export_calendar
import xhutimetable.composeapp.generated.resources.ic_action_holiday
import xhutimetable.composeapp.generated.resources.ic_action_multi_user
import xhutimetable.composeapp.generated.resources.ic_action_night_mode
import xhutimetable.composeapp.generated.resources.ic_action_notify_course
import xhutimetable.composeapp.generated.resources.ic_action_notify_exam
import xhutimetable.composeapp.generated.resources.ic_action_notify_time
import xhutimetable.composeapp.generated.resources.ic_action_qq_group
import xhutimetable.composeapp.generated.resources.ic_action_school_calendar
import xhutimetable.composeapp.generated.resources.ic_action_search
import xhutimetable.composeapp.generated.resources.ic_action_send
import xhutimetable.composeapp.generated.resources.ic_action_show_status
import xhutimetable.composeapp.generated.resources.ic_action_switch
import xhutimetable.composeapp.generated.resources.ic_action_view
import xhutimetable.composeapp.generated.resources.ic_add
import xhutimetable.composeapp.generated.resources.ic_add_circle
import xhutimetable.composeapp.generated.resources.ic_admin_status
import xhutimetable.composeapp.generated.resources.ic_back
import xhutimetable.composeapp.generated.resources.ic_calendar
import xhutimetable.composeapp.generated.resources.ic_calendar_night
import xhutimetable.composeapp.generated.resources.ic_calendar_unchecked
import xhutimetable.composeapp.generated.resources.ic_calendar_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_checked
import xhutimetable.composeapp.generated.resources.ic_class_settings
import xhutimetable.composeapp.generated.resources.ic_class_settings_night
import xhutimetable.composeapp.generated.resources.ic_classroom
import xhutimetable.composeapp.generated.resources.ic_classroom_night
import xhutimetable.composeapp.generated.resources.ic_corner_failed
import xhutimetable.composeapp.generated.resources.ic_corner_success
import xhutimetable.composeapp.generated.resources.ic_enable_calendar_view
import xhutimetable.composeapp.generated.resources.ic_exam
import xhutimetable.composeapp.generated.resources.ic_exam_night
import xhutimetable.composeapp.generated.resources.ic_exp_score
import xhutimetable.composeapp.generated.resources.ic_exp_score_night
import xhutimetable.composeapp.generated.resources.ic_feedback
import xhutimetable.composeapp.generated.resources.ic_feedback_night
import xhutimetable.composeapp.generated.resources.ic_github
import xhutimetable.composeapp.generated.resources.ic_hot
import xhutimetable.composeapp.generated.resources.ic_job
import xhutimetable.composeapp.generated.resources.ic_job_night
import xhutimetable.composeapp.generated.resources.ic_join_group
import xhutimetable.composeapp.generated.resources.ic_join_group_night
import xhutimetable.composeapp.generated.resources.ic_notice
import xhutimetable.composeapp.generated.resources.ic_notice_night
import xhutimetable.composeapp.generated.resources.ic_poems
import xhutimetable.composeapp.generated.resources.ic_profile
import xhutimetable.composeapp.generated.resources.ic_profile_night
import xhutimetable.composeapp.generated.resources.ic_profile_unchecked
import xhutimetable.composeapp.generated.resources.ic_profile_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_radius_cell
import xhutimetable.composeapp.generated.resources.ic_score
import xhutimetable.composeapp.generated.resources.ic_score_night
import xhutimetable.composeapp.generated.resources.ic_server_detect
import xhutimetable.composeapp.generated.resources.ic_server_detect_night
import xhutimetable.composeapp.generated.resources.ic_settings
import xhutimetable.composeapp.generated.resources.ic_settings_night
import xhutimetable.composeapp.generated.resources.ic_share
import xhutimetable.composeapp.generated.resources.ic_share_night
import xhutimetable.composeapp.generated.resources.ic_show_not_this_week
import xhutimetable.composeapp.generated.resources.ic_sync
import xhutimetable.composeapp.generated.resources.ic_today_course
import xhutimetable.composeapp.generated.resources.ic_today_course_night
import xhutimetable.composeapp.generated.resources.ic_today_course_unchecked
import xhutimetable.composeapp.generated.resources.ic_today_course_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_today_course_watermelon
import xhutimetable.composeapp.generated.resources.ic_unknown_menu
import xhutimetable.composeapp.generated.resources.ic_unknown_menu_night
import xhutimetable.composeapp.generated.resources.ic_urge
import xhutimetable.composeapp.generated.resources.ic_urge_night
import xhutimetable.composeapp.generated.resources.ic_user_campus
import xhutimetable.composeapp.generated.resources.ic_week_course
import xhutimetable.composeapp.generated.resources.ic_week_course_night
import xhutimetable.composeapp.generated.resources.ic_week_course_unchecked
import xhutimetable.composeapp.generated.resources.ic_week_course_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_week_view
import xhutimetable.composeapp.generated.resources.ic_ws_state_connected
import xhutimetable.composeapp.generated.resources.ic_ws_state_connecting
import xhutimetable.composeapp.generated.resources.ic_ws_state_disconnected
import xhutimetable.composeapp.generated.resources.ic_ws_state_failed
import xhutimetable.composeapp.generated.resources.img_boy1
import xhutimetable.composeapp.generated.resources.img_boy2
import xhutimetable.composeapp.generated.resources.img_boy3
import xhutimetable.composeapp.generated.resources.img_boy4
import xhutimetable.composeapp.generated.resources.img_girl1
import xhutimetable.composeapp.generated.resources.img_girl2
import xhutimetable.composeapp.generated.resources.img_girl3
import xhutimetable.composeapp.generated.resources.img_girl4
import xhutimetable.composeapp.generated.resources.main_bg

@Composable
private fun iconOf(pair: Pair<Painter, Painter>): Painter =
    if (isDarkMode()) pair.first else pair.second

object XhuIcons {
    val todayWaterMelon: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_today_course_watermelon)
    val conflict: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_radius_cell)
    val back: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_back)
    val add: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_add)
    val multiUser: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_multi_user)
    val showNotThisWeek: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_show_not_this_week)
    val showCourseStatus: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_show_status)
    val customYearTerm: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_year_term)
    val customStartTime: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_start_time)
    val userCampus: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_user_campus)
    val customCourseColor: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_course_color)
    val schoolCalendar: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_school_calendar)
    val exportCalendar: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_export_calendar)
    val customCourse: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_course)
    val customThing: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_thing)
    val customBackground: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_background)
    val customUi: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_custom_ui)
    val disableBackgroundWhenNight: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_disable_when_night)
    val enableCalendarView: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_enable_calendar_view)
    val nightMode: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_night_mode)
    val notifyCourse: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_notify_course)
    val holiday: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_holiday)
    val notifyExam: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_notify_exam)
    val notifyTime: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_notify_time)
    val checkUpdate: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_check_update)
    val allowUploadCrash: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_allow_upload_crash)
    val qqGroup: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_qq_group)
    val poems: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_poems)
    val checked: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_checked)
    val close: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_close)
    val clearSplash: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_action_clear_splash)
    val github: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_github)
    val cornerSuccess: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_corner_success)
    val cornerFailed: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_corner_failed)
    val hot: Painter
        @Composable
        get() = painterResource(Res.drawable.ic_hot)

    object Profile {
        val exam: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_exam_night) to
                        painterResource(Res.drawable.ic_exam)
            )
        val score: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_score_night) to
                        painterResource(Res.drawable.ic_score)
            )
        val classroom: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_classroom_night) to
                        painterResource(Res.drawable.ic_classroom)
            )
        val accountSettings: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_account_settings_night) to
                        painterResource(Res.drawable.ic_account_settings)
            )
        val classSettings: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_class_settings_night) to
                        painterResource(Res.drawable.ic_class_settings)
            )
        val settings: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_settings_night) to
                        painterResource(Res.drawable.ic_settings)
            )
        val notice: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_notice_night) to
                        painterResource(Res.drawable.ic_notice)
            )
        val feedback: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_feedback_night) to
                        painterResource(Res.drawable.ic_feedback)
            )
        val share: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_share_night) to
                        painterResource(Res.drawable.ic_share)
            )
        val urge: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_urge_night) to
                        painterResource(Res.drawable.ic_urge)
            )
        val expScore: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_exp_score_night) to
                        painterResource(Res.drawable.ic_exp_score)
            )
        val serverDetect: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_server_detect_night) to
                        painterResource(Res.drawable.ic_server_detect)
            )
        val job: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_job_night) to
                        painterResource(Res.drawable.ic_job)
            )
        val joinGroup: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_join_group_night) to
                        painterResource(Res.drawable.ic_join_group)
            )
        val unknownMenu: Painter
            @Composable
            get() = iconOf(
                painterResource(Res.drawable.ic_unknown_menu_night) to
                        painterResource(Res.drawable.ic_unknown_menu)
            )
    }

    object Action {
        val done: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_action_done)
        val view: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_action_view)
        val send: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_action_send)
        val search: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_action_search)
        val addCircle: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_add_circle)
        val sync: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_sync)
        val switch: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_action_switch)
        val weekView: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_week_view)
    }

    object WsState {
        val connected: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_ws_state_connected)
        val connecting: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_ws_state_connecting)
        val disconnected: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_ws_state_disconnected)
        val failed: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_ws_state_failed)
        val adminOnline: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_admin_status)
    }
}

@Composable
fun stateOf(
    checked: Boolean,
    pair: Pair<Pair<DrawableResource, DrawableResource>, Pair<DrawableResource, DrawableResource>>
): Painter {
    val (checkedId, uncheckedId) = if (isDarkMode()) pair.first else pair.second
    return if (checked) painterResource(checkedId) else painterResource(uncheckedId)
}

object XhuStateIcons {
    val todayCourse =
        (Res.drawable.ic_today_course_night to Res.drawable.ic_today_course_unchecked_night) to
                (Res.drawable.ic_today_course to Res.drawable.ic_today_course_unchecked)
    val weekCourse =
        (Res.drawable.ic_week_course_night to Res.drawable.ic_week_course_unchecked_night) to
                (Res.drawable.ic_week_course to Res.drawable.ic_week_course_unchecked)
    val calendar =
        (Res.drawable.ic_calendar_night to Res.drawable.ic_calendar_unchecked_night) to
                (Res.drawable.ic_calendar to Res.drawable.ic_calendar_unchecked)
    val profile = (Res.drawable.ic_profile_night to Res.drawable.ic_profile_unchecked_night) to
            (Res.drawable.ic_profile to Res.drawable.ic_profile_unchecked)
}

object XhuImages {
    val defaultBackgroundImage: DrawableResource
        get() = Res.drawable.main_bg
    val defaultBackgroundImageUri: String
        get() = Res.getUri("drawable/main_bg.png")
    val defaultProfileImage: String
        get() = Res.getUri("drawable/icon.xml")
}

object ProfileImages {
    private val boyPool = listOf(
        Res.drawable.img_boy1,
        Res.drawable.img_boy2,
        Res.drawable.img_boy3,
        Res.drawable.img_boy4,
    )
    private val girlPool = listOf(
        Res.drawable.img_girl1,
        Res.drawable.img_girl2,
        Res.drawable.img_girl3,
        Res.drawable.img_girl4,
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