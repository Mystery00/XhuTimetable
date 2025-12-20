package vip.mystery0.xhu.timetable.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.glance.unit.ColorProvider
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.ui.theme.DarkColorScheme
import vip.mystery0.xhu.timetable.ui.theme.LightColorScheme
import vip.mystery0.xhu.timetable.ui.theme.NightMode

data class WidgetColors(
    val primary: ColorProvider,
    val onPrimary: ColorProvider,
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val secondary: ColorProvider,
    val onSecondary: ColorProvider,
)

object WidgetTheme {
    fun getColors(context: Context): WidgetColors {
        val mode = GlobalConfigStore.nightMode
        val isSystemDark =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (mode) {
            NightMode.AUTO, NightMode.MATERIAL_YOU -> isSystemDark
            NightMode.ON -> true
            NightMode.OFF -> false
        }

        val scheme =
            if (mode == NightMode.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) DarkColorScheme else LightColorScheme
            }

        return createColors(scheme)
    }

    @SuppressLint("RestrictedApi")
    private fun createColors(scheme: ColorScheme): WidgetColors {
        return WidgetColors(
            primary = ColorProvider(scheme.primary),
            onPrimary = ColorProvider(scheme.onPrimary),
            surface = ColorProvider(scheme.surface),
            onSurface = ColorProvider(scheme.onSurface),
            secondary = ColorProvider(scheme.secondary),
            onSecondary = ColorProvider(scheme.onSecondary),
        )
    }
}
