package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.model.entity.NightMode

private val DarkColorPalette = darkColors()

private val LightColorPalette = lightColors(
    primary = Color(0xFF2196F3),
    primaryVariant = Color(0xFF1976D2),
    secondary = Color(0xFF4CAF50),
    secondaryVariant = Color(0xFF2196F3),
)

@Composable
fun XhuTimetableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = when (DataHolder.nightMode) {
        NightMode.AUTO -> if (darkTheme) {
            DarkColorPalette
        } else {
            LightColorPalette
        }
        NightMode.ON -> DarkColorPalette
        NightMode.OFF -> LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        content = content
    )
}