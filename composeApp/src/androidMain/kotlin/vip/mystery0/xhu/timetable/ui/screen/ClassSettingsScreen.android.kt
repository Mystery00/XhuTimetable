package vip.mystery0.xhu.timetable.ui.screen

import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import vip.mystery0.xhu.timetable.ui.activity.ExportCalendarActivity
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

@Composable
actual fun ExportToCalendarSettings() {
    val context = LocalContext.current
    XhuSettingsMenuLink(
        icon = {
            Icon(
                painter = XhuIcons.exportCalendar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
        title = { Text(text = "导出到日历") },
        onClick = {
            context.startActivity(Intent(context, ExportCalendarActivity::class.java))
        }
    )
}