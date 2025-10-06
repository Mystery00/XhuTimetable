package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.config.toast.showLongToast
import vip.mystery0.xhu.timetable.model.event.MenuNavigator
import vip.mystery0.xhu.timetable.ui.navigation.RouteAccountManagement
import vip.mystery0.xhu.timetable.ui.navigation.RouteClassSettings
import vip.mystery0.xhu.timetable.ui.navigation.RouteFeedback
import vip.mystery0.xhu.timetable.ui.navigation.RouteFreeCourseRoom
import vip.mystery0.xhu.timetable.ui.navigation.RouteNotice
import vip.mystery0.xhu.timetable.ui.navigation.RouteQueryExam
import vip.mystery0.xhu.timetable.ui.navigation.RouteQueryExpScore
import vip.mystery0.xhu.timetable.ui.navigation.RouteQueryScore
import vip.mystery0.xhu.timetable.ui.navigation.RouteSchoolTimetable
import vip.mystery0.xhu.timetable.ui.navigation.RouteSettings
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

enum class MenuItem(
    val icon: @Composable () -> Painter,
    val action: suspend MenuNavigator.(Menu) -> Unit = {},
) {
    QUERY_EXAM(
        { XhuIcons.Profile.exam },
        { navigateTo(RouteQueryExam) },
    ),
    QUERY_SCORE(
        { XhuIcons.Profile.score },
        { navigateTo(RouteQueryScore) }
    ),
    QUERY_FREE_ROOM(
        { XhuIcons.Profile.classroom },
        { navigateTo(RouteFreeCourseRoom) }
    ),
    QUERY_TIMETABLE(
        { XhuIcons.Profile.schoolTimetable },
        { navigateTo(RouteSchoolTimetable) }
    ),
    ACCOUNT_MANAGE(
        { XhuIcons.Profile.accountSettings },
        { navigateTo(RouteAccountManagement) }
    ),
    CLASS_SETTING(
        { XhuIcons.Profile.classSettings },
        { navigateTo(RouteClassSettings) }
    ),
    SETTINGS(
        { XhuIcons.Profile.settings },
        { navigateTo(RouteSettings) }
    ),
    NOTICE(
        { XhuIcons.Profile.notice },
        { navigateTo(RouteNotice) }
    ),
    FEEDBACK(
        { XhuIcons.Profile.feedback },
        { navigateTo(RouteFeedback) }
    ),
    SHARE(
        { XhuIcons.Profile.share },
        { showSharePanel(shareText.random()) }
    ),
    URGE(
        { XhuIcons.Profile.urge },
//        { intentTo(UrgeActivity::class) }
    ),
    EXP_SCORE(
        { XhuIcons.Profile.expScore },
        { navigateTo(RouteQueryExpScore) }
    ),
    SERVER_DETECT(
        { XhuIcons.Profile.serverDetect },
        {
            toCustomTabs(it.link)
        },
    ),
    JOB(
        { XhuIcons.Profile.job },
//        { intentTo(JobHistoryActivity::class) },
    ),
    JOIN_GROUP(
        { XhuIcons.Profile.joinGroup },
        { toCustomTabs(it.link) },
    ),
    EMPTY(
        { XhuIcons.Profile.unknownMenu },
        { menu ->
            if (menu.hint.isBlank() && menu.link.isNotBlank()) {
                toCustomTabs(menu.link)
            } else {
                if (menu.hint.isNotBlank()) {
                    showLongToast(menu.hint)
                } else {
                    showLongToast("当前版本暂不支持该功能，请更新到最新版本")
                }
            }
        }
    )
    ;

    companion object {
        fun parseKey(key: String): MenuItem {
            for (value in entries) {
                if (value.name.equals(key, true)) {
                    return value
                }
            }
            return EMPTY
        }
    }
}

private val shareText = arrayListOf(
    "查课查成绩，我就用西瓜课表~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜子都在用的课表，最方便的学习助手~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜课表，轻松查课，快速查成绩~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜子们的必备神器，下载西瓜课表，追求高效学习~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜课表，移动学习的首选工具，随时随地掌握课程~ 下载链接：https://xgkb.mystery0.vip",
)