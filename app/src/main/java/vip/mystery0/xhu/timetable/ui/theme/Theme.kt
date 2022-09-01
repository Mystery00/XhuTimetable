package vip.mystery0.xhu.timetable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.model.entity.NightMode

private val DarkColorPalette = darkColors()

private val LightColorPalette = lightColors(
    primary = Color(0xFF2196F3),
    primaryVariant = Color(0xFF1976D2),
    secondary = Color(0xFF4CAF50),
    secondaryVariant = Color(0xFF2196F3),
)

@Composable
fun isDarkMode(): Boolean {
    val mode by Theme.nightMode.collectAsState()
//    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    return when (mode) {
        NightMode.AUTO -> isSystemInDarkTheme()
        NightMode.ON -> true
        NightMode.OFF -> false
        NightMode.MATERIAL_YOU -> isSystemInDarkTheme()
    }
}

@Composable
fun XhuTimetableTheme(
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode()) DarkColorPalette else LightColorPalette

    val replacementTypography = Typography(
        defaultFontFamily = XhuFonts.custom ?: FontFamily.Default
    )
    MaterialTheme(
        typography = replacementTypography,
        colors = colors,
        content = content
    )
}

object Theme {
    val nightMode = MutableStateFlow(GlobalConfig.nightMode)
}