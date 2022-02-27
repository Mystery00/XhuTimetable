package vip.mystery0.xhu.timetable.model.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import vip.mystery0.xhu.timetable.ui.activity.MainActivity

enum class MenuItem(
    val icon: @Composable () -> Painter,
    val action: MainActivity.() -> Unit,
) {
    QUERY_EXAM(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.exam },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.ExamActivity::class) },
    ),
    QUERY_SCORE(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.score },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.ScoreActivity::class) },
    ),
    QUERY_FREE_ROOM(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.classroom },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.CourseRoomActivity::class) },
    ),
    ACCOUNT_MANAGE(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.accountSettings },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.AccountSettingsActivity::class) },
    ),
    CLASS_SETTING(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.classSettings },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.ClassSettingsActivity::class) },
    ),
    SETTINGS(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.settings },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.SettingsActivity::class) },
    ),
    NOTICE(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.notice },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.NoticeActivity::class) },
    ),
    FEEDBACK(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.feedback },
        { intentTo(vip.mystery0.xhu.timetable.ui.activity.FeedbackActivity::class) },
    ),
    SHARE(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.share },
        {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                putExtra(android.content.Intent.EXTRA_TEXT, shareText.random())
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "分享西瓜课表到"))
        },
    ),
    EMPTY(
        { vip.mystery0.xhu.timetable.ui.theme.XhuIcons.Profile.unknownMenu },
        {}
    )
    ;

    companion object {
        fun parseKey(key: String): MenuItem {
            for (value in values()) {
                if (value.name.equals(key, true)) {
                    return value
                }
            }
            return EMPTY
        }
    }
}

private val shareText = arrayListOf(
    "查课查课表，我就用西瓜课表~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜子都在用的课表~ 下载链接：https://xgkb.mystery0.vip"
)