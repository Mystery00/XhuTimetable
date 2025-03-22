package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

data class XhuColorProvider(val color: Color) : ColorProvider {
    override fun getColor(context: Context) = color
}