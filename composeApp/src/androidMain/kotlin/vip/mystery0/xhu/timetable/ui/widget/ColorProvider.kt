package vip.mystery0.xhu.timetable.ui.widget

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.FixedColorProvider

@SuppressLint("RestrictedApi")
fun ColorProvider(color: Color): ColorProvider {
    return FixedColorProvider(color)
}