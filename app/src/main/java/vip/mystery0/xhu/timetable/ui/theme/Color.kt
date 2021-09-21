package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import vip.mystery0.xhu.timetable.R

object XhuColor {
    val splashBackground: Color
        @Composable
        get() = colorResource(id = R.color.splash_color_background)
    val loginLabel: Color
        @Composable
        get() = colorResource(id = R.color.login_color_label)
    val loginText: Color
        @Composable
        get() = colorResource(id = R.color.login_color_text)
}