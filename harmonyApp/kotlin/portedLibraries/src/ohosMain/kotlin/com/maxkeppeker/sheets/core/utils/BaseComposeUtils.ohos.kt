package com.maxkeppeker.sheets.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp

@Composable
actual fun isLandscape(): Boolean =
    LocalWindowInfo.current.containerSize.width >= with(LocalDensity.current) { 840.dp.toPx() }
