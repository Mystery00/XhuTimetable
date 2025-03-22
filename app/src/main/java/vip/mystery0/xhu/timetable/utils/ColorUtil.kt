package vip.mystery0.xhu.timetable.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.util.Locale

fun Color.toHexString(
    locale: Locale = Locale.CHINA
): String {
    val convert = android.graphics.Color.valueOf(red, green, blue)
    return "#${Integer.toHexString(convert.toArgb()).uppercase(locale)}"
}

fun String.parseColorHexString(): Color {
    if (this.isBlank()) {
        return ColorPool.random
    }
    val color = this.toColorInt()
    return Color(color)
}