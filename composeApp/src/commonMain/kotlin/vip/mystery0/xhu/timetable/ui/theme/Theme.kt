package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts.globalSet

@Composable
expect fun getColorScheme(): ColorScheme

@Composable
fun isDarkMode(): Boolean {
    val mode by Theme.nightMode.collectAsState()
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
    val colorScheme = getColorScheme()

    val replacementTypography = MaterialTheme.typography.globalSet(XhuFonts.DEFAULT)

    MaterialExpressiveTheme(
        typography = replacementTypography,
        colorScheme = colorScheme,
        content = content,
    )
}

object Theme {
    val nightMode = MutableStateFlow(GlobalConfigStore.nightMode)
}