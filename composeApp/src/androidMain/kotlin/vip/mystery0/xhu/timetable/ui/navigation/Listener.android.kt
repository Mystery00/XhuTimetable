package vip.mystery0.xhu.timetable.ui.navigation

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import vip.mystery0.xhu.timetable.ui.activity.NavActivity
import vip.mystery0.xhu.timetable.ui.activity.NavActivity.Companion.jumpIntent

private fun pushDynamicShortcuts(
    context: Context,
    @DrawableRes iconResId: Int,
    label: String,
    id: String = "shortcut_$label",
) {
    val shortcut = ShortcutInfoCompat.Builder(context, id)
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(IconCompat.createWithResource(context, iconResId))
        .setIntent(jumpIntent(context, NavActivity.InitRoute.EXAM.name))
        .build()
    ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
}