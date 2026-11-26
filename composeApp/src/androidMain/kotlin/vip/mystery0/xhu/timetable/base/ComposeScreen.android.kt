package vip.mystery0.xhu.timetable.base

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun HandleBackPress(enabled: Boolean, backPressed: () -> Unit) {
    BackHandler(enabled = enabled, onBack = backPressed)
}