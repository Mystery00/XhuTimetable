package vip.mystery0.xhu.timetable.model.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.toCustomTabs
import vip.mystery0.xhu.timetable.ui.activity.AccountSettingsActivity
import vip.mystery0.xhu.timetable.ui.activity.ClassSettingsActivity
import vip.mystery0.xhu.timetable.ui.activity.CourseRoomActivity
import vip.mystery0.xhu.timetable.ui.activity.ExamActivity
import vip.mystery0.xhu.timetable.ui.activity.ExpScoreActivity
import vip.mystery0.xhu.timetable.ui.activity.FeedbackActivity
import vip.mystery0.xhu.timetable.ui.activity.JobHistoryActivity
import vip.mystery0.xhu.timetable.ui.activity.MainActivity
import vip.mystery0.xhu.timetable.ui.activity.NoticeActivity
import vip.mystery0.xhu.timetable.ui.activity.ScoreActivity
import vip.mystery0.xhu.timetable.ui.activity.SettingsActivity
import vip.mystery0.xhu.timetable.ui.activity.UrgeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

enum class MenuItem(
    val icon: @Composable () -> Painter,
    val action: suspend MainActivity.(Menu) -> Unit,
) {
    QUERY_EXAM(
        { XhuIcons.Profile.exam },
        { intentTo(ExamActivity::class) },
    ),
    QUERY_SCORE(
        { XhuIcons.Profile.score },
        { intentTo(ScoreActivity::class) },
    ),
    QUERY_FREE_ROOM(
        { XhuIcons.Profile.classroom },
        { intentTo(CourseRoomActivity::class) },
    ),
    ACCOUNT_MANAGE(
        { XhuIcons.Profile.accountSettings },
        { intentTo(AccountSettingsActivity::class) },
    ),
    CLASS_SETTING(
        { XhuIcons.Profile.classSettings },
        { intentTo(ClassSettingsActivity::class) },
    ),
    SETTINGS(
        { XhuIcons.Profile.settings },
        { intentTo(SettingsActivity::class) },
    ),
    NOTICE(
        { XhuIcons.Profile.notice },
        { intentTo(NoticeActivity::class) },
    ),
    FEEDBACK(
        { XhuIcons.Profile.feedback },
        { intentTo(FeedbackActivity::class) },
    ),
    SHARE(
        { XhuIcons.Profile.share },
        {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                putExtra(android.content.Intent.EXTRA_TEXT, shareText.random())
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "分享西瓜课表到"))
        },
    ),
    URGE(
        { XhuIcons.Profile.urge },
        { intentTo(UrgeActivity::class) }
    ),
    EXP_SCORE(
        { XhuIcons.Profile.expScore },
        { intentTo(ExpScoreActivity::class) },
    ),
    SERVER_DETECT(
        { XhuIcons.Profile.serverDetect },
        { toCustomTabs(it.link) },
    ),
    JOB(
        { XhuIcons.Profile.job },
        { intentTo(JobHistoryActivity::class) },
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
                    toastString(menu.hint, true)
                } else {
                    toastString("当前版本暂不支持该功能，请更新到最新版本", true)
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