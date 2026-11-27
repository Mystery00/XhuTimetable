package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.utils.md5
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_account_settings
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
import xhutimetable.composeapp.generated.resources.ic_classroom
import xhutimetable.composeapp.generated.resources.ic_enable_calendar_view
import xhutimetable.composeapp.generated.resources.ic_exam
import xhutimetable.composeapp.generated.resources.ic_exp_score
import xhutimetable.composeapp.generated.resources.ic_feedback
import xhutimetable.composeapp.generated.resources.ic_github
import xhutimetable.composeapp.generated.resources.ic_job
import xhutimetable.composeapp.generated.resources.ic_join_group
import xhutimetable.composeapp.generated.resources.ic_notice
import xhutimetable.composeapp.generated.resources.ic_poems
import xhutimetable.composeapp.generated.resources.ic_practical_course
import xhutimetable.composeapp.generated.resources.ic_profile
import xhutimetable.composeapp.generated.resources.ic_profile_night
import xhutimetable.composeapp.generated.resources.ic_profile_unchecked
import xhutimetable.composeapp.generated.resources.ic_profile_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_radius_cell
import xhutimetable.composeapp.generated.resources.ic_school_timetable
import xhutimetable.composeapp.generated.resources.ic_score
import xhutimetable.composeapp.generated.resources.ic_server_detect
import xhutimetable.composeapp.generated.resources.ic_settings
import xhutimetable.composeapp.generated.resources.ic_share
import xhutimetable.composeapp.generated.resources.ic_show_not_this_week
import xhutimetable.composeapp.generated.resources.ic_sync
import xhutimetable.composeapp.generated.resources.ic_today_course
import xhutimetable.composeapp.generated.resources.ic_today_course_night
import xhutimetable.composeapp.generated.resources.ic_today_course_unchecked
import xhutimetable.composeapp.generated.resources.ic_today_course_unchecked_night
import xhutimetable.composeapp.generated.resources.ic_today_course_watermelon
import xhutimetable.composeapp.generated.resources.ic_unknown_menu
import xhutimetable.composeapp.generated.resources.ic_urge
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

    object Profile {
        val exam: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_exam)
        val score: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_score)
        val classroom: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_classroom)
        val schoolTimetable: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_school_timetable)
        val accountSettings: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_account_settings)
        val classSettings: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_class_settings)
        val settings: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_settings)
        val notice: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_notice)
        val feedback: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_feedback)
        val share: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_share)
        val urge: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_urge)
        val expScore: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_exp_score)
        val serverDetect: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_server_detect)
        val job: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_job)
        val joinGroup: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_join_group)
        val unknownMenu: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_unknown_menu)
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
        val practicalCourse: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_practical_course)
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

data class BuiltinAvatar(
    val key: String,
    val label: String,
    val resource: DrawableResource,
)

object ProfileImages {
    const val BUILTIN_PREFIX = "builtin:"
    const val FILE_PREFIX = "file:"

    val builtinAvatars: List<BuiltinAvatar> = listOf(
        BuiltinAvatar("boy1", "清爽学长", Res.drawable.img_boy1),
        BuiltinAvatar("boy2", "阳光运动", Res.drawable.img_boy2),
        BuiltinAvatar("boy3", "文艺书生", Res.drawable.img_boy3),
        BuiltinAvatar("boy4", "活力少年", Res.drawable.img_boy4),
        BuiltinAvatar("girl1", "温柔学姐", Res.drawable.img_girl1),
        BuiltinAvatar("girl2", "元气少女", Res.drawable.img_girl2),
        BuiltinAvatar("girl3", "知性清新", Res.drawable.img_girl3),
        BuiltinAvatar("girl4", "软萌可人", Res.drawable.img_girl4),
    )

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

    private val _customAvatarFlow = MutableStateFlow(GlobalConfigStore.customAvatar)
    val customAvatarFlow: StateFlow<String> = _customAvatarFlow

    fun updateCustomAvatar(value: String) {
        _customAvatarFlow.value = value
    }

    fun findBuiltin(key: String): BuiltinAvatar? = builtinAvatars.firstOrNull { it.key == key }

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

@Composable
fun UserAvatarImage(
    userName: String?,
    isMale: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val customAvatar by ProfileImages.customAvatarFlow.collectAsState()
    val fallbackPainter = if (!userName.isNullOrBlank()) {
        ProfileImages.hash(userName, isMale)
    } else {
        rememberAsyncImagePainter(model = XhuImages.defaultProfileImage)
    }

    when {
        customAvatar.startsWith(ProfileImages.FILE_PREFIX) -> {
            val filePath = customAvatar.removePrefix(ProfileImages.FILE_PREFIX)
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(filePath)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                placeholder = fallbackPainter,
                error = fallbackPainter,
                modifier = modifier,
            )
        }

        customAvatar.startsWith(ProfileImages.BUILTIN_PREFIX) -> {
            val key = customAvatar.removePrefix(ProfileImages.BUILTIN_PREFIX)
            val builtin = ProfileImages.findBuiltin(key)
            Image(
                painter = if (builtin != null) painterResource(builtin.resource) else fallbackPainter,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        }

        else -> {
            Image(
                painter = fallbackPainter,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        }
    }
}