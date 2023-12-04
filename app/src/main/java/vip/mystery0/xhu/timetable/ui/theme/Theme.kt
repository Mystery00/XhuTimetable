package vip.mystery0.xhu.timetable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts.globalSet

private val DarkColorPalette = darkColorScheme()

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF2196F3),
    primaryContainer = Color(0xFF1976D2),
    secondary = Color(0xFF4CAF50),
    secondaryContainer = Color(0xFF2196F3),
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
    val isDark = isDarkMode()
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = if (dynamicColor) {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) DarkColorPalette else LightColorPalette
    }

    val replacementTypography = MaterialTheme.typography.globalSet(XhuFonts.DEFAULT)

    val extendedColors = ExtendedColors(
        surfaceContainer = colorScheme.surfaceColorAtElevation(4.dp)
    )
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            typography = replacementTypography,
            colorScheme = colorScheme,
            content = content,
        )
    }
}

object Theme {
    val nightMode = MutableStateFlow(GlobalConfigStore.nightMode)
}