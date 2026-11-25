package me.zhanghai.compose.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
actual fun createDefaultPreferenceFlow(): MutableStateFlow<Preferences> =
    remember { MutableStateFlow(MapPreferences()) }
