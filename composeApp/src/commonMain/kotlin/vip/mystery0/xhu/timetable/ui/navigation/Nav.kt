package vip.mystery0.xhu.timetable.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.ui.screen.AboutScreen
import vip.mystery0.xhu.timetable.ui.screen.AccountManagementScreen
import vip.mystery0.xhu.timetable.ui.screen.BackgroundScreen
import vip.mystery0.xhu.timetable.ui.screen.ClassSettingsScreen
import vip.mystery0.xhu.timetable.ui.screen.CustomCourseColorScreen
import vip.mystery0.xhu.timetable.ui.screen.CustomCourseScreen
import vip.mystery0.xhu.timetable.ui.screen.CustomThingScreen
import vip.mystery0.xhu.timetable.ui.screen.CustomUiScreen
import vip.mystery0.xhu.timetable.ui.screen.FeedbackScreen
import vip.mystery0.xhu.timetable.ui.screen.FreeCourseRoomScreen
import vip.mystery0.xhu.timetable.ui.screen.InitScreen
import vip.mystery0.xhu.timetable.ui.screen.LoginScreen
import vip.mystery0.xhu.timetable.ui.screen.MainScreen
import vip.mystery0.xhu.timetable.ui.screen.NoticeScreen
import vip.mystery0.xhu.timetable.ui.screen.QueryExamScreen
import vip.mystery0.xhu.timetable.ui.screen.QueryExpScoreScreen
import vip.mystery0.xhu.timetable.ui.screen.QueryScoreScreen
import vip.mystery0.xhu.timetable.ui.screen.SchoolCalendarScreen
import vip.mystery0.xhu.timetable.ui.screen.SchoolTimetableScreen
import vip.mystery0.xhu.timetable.ui.screen.SettingsScreen
import vip.mystery0.xhu.timetable.ui.screen.SplashImageScreen

val LocalNavController = compositionLocalOf<NavController?> { null }

interface Nav

@Serializable
object RouteInit : Nav

@Serializable
data class RouteSplashImage(
    val splashFilePath: String?,
    val splashId: Long,
) : Nav

@Serializable
object RouteMain : Nav

@Serializable
object RouteQueryExam : Nav

@Serializable
object RouteQueryScore : Nav

@Serializable
object RouteQueryExpScore : Nav

@Serializable
object RouteAccountManagement : Nav

@Serializable
object RouteClassSettings : Nav

@Serializable
object RouteCustomCourseColor : Nav

@Serializable
object RouteSettings : Nav

@Serializable
object RouteCustomUi : Nav

@Serializable
data class RouteLogin(val fromAccountManager: Boolean) : Nav

@Serializable
object RouteNotice : Nav

@Serializable
object RouteBackground : Nav

@Serializable
object RouteCustomThing : Nav

@Serializable
object RouteCustomCourse : Nav

@Serializable
object RouteFeedback : Nav

@Serializable
object RouteFreeCourseRoom : Nav

@Serializable
object RouteAbout : Nav

@Serializable
object RouteSchoolCalendar : Nav

@Serializable
object RouteSchoolTimetable : Nav

val Navs: NavGraphBuilder.() -> Unit = {
    composable<RouteLogin> { backStackEntry ->
        val login: RouteLogin = backStackEntry.toRoute()
        LoginScreen(login.fromAccountManager)
    }
    composable<RouteInit> { InitScreen() }
    composable<RouteSplashImage> { backStackEntry ->
        val splash: RouteSplashImage = backStackEntry.toRoute()
        SplashImageScreen(splash.splashFilePath, splash.splashId)
    }
    composable<RouteMain> { MainScreen() }
    composable<RouteQueryExam> { QueryExamScreen() }
    composable<RouteQueryScore> { QueryScoreScreen() }
    composable<RouteQueryExpScore> { QueryExpScoreScreen() }
    composable<RouteAccountManagement> { AccountManagementScreen() }
    composable<RouteClassSettings> { ClassSettingsScreen() }
    composable<RouteCustomCourseColor> { CustomCourseColorScreen() }
    composable<RouteSettings> { SettingsScreen() }
    composable<RouteCustomUi> { CustomUiScreen() }
    composable<RouteNotice> { NoticeScreen() }
    composable<RouteBackground> { BackgroundScreen() }
    composable<RouteCustomThing> { CustomThingScreen() }
    composable<RouteCustomCourse> { CustomCourseScreen() }
    composable<RouteFeedback> { FeedbackScreen() }
    composable<RouteFreeCourseRoom> { FreeCourseRoomScreen() }
    composable<RouteAbout> { AboutScreen() }
    composable<RouteSchoolCalendar> { SchoolCalendarScreen() }
    composable<RouteSchoolTimetable> { SchoolTimetableScreen() }
}

inline fun <reified F : Any> NavController.replaceTo(target: Any) {
    navigate(target) {
        popUpTo(F::class) {
            inclusive = true
        }
    }
}

fun NavController.navigateAndSave(target: Any) {
    navigate(target)
}