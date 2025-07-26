package vip.mystery0.xhu.timetable.ui.component

import android.content.Intent
import coil3.PlatformContext

actual fun showSharePanel(context: PlatformContext, shareText: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享西瓜课表到"))
}