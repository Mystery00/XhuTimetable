package vip.mystery0.xhu.timetable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.ui.theme.XhuFonts.globalSet

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
private fun getColorScheme(): ColorScheme {
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

@Composable
fun XhuTimetableTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme()

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