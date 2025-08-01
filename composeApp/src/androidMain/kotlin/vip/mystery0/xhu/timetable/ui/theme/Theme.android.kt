package vip.mystery0.xhu.timetable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getColorScheme(): ColorScheme {
    val mode by Theme.nightMode.collectAsState()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    when (mode) {
        NightMode.MATERIAL_YOU -> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //满足Material You条件
                val context = LocalContext.current
                if (isSystemInDarkTheme)
                    dynamicDarkColorScheme(context)
                else
                    dynamicLightColorScheme(context)
            } else {
                //不满足Material You条件，降级为自动
                if (isSystemInDarkTheme) {
                    DarkColorScheme
                } else {
                    LightColorScheme
                }
            }
        }

        //强制开启夜间模式
        NightMode.ON -> return DarkColorScheme
        //强制关闭夜间模式
        NightMode.OFF -> return LightColorScheme

        NightMode.AUTO -> {
            return if (isSystemInDarkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }
}