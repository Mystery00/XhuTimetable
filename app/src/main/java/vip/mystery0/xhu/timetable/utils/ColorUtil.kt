package vip.mystery0.xhu.timetable.utils

import androidx.compose.ui.graphics.Color
import java.util.Locale

fun Color.toHexString(
    locale: Locale = Locale.CHINA
): String {
    val convert = android.graphics.Color.valueOf(red, green, blue)
    return "#${Integer.toHexString(convert.toArgb()).uppercase(locale)}"
}

fun String.parseColorHexString(): Color {
    val color = android.graphics.Color.parseColor(this)
    return Color(color)
}