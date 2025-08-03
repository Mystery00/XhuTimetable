package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SetSystemAppearance(isDark: Boolean) {
    LaunchedEffect(isDark) {
        SystemAppearanceManager.isDarkState.value = isDark
    }
}

object SystemAppearanceManager {
    val isDarkState = MutableStateFlow(false)
}