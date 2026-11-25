package vip.mystery0.xhu.timetable.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext

fun ComponentActivity.applyImmersiveEdgeToEdge(isDark: Boolean) {
    val statusBarStyle = if (isDark) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    val navigationBarStyle = if (isDark) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(
        statusBarStyle = statusBarStyle,
        navigationBarStyle = navigationBarStyle,
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.isStatusBarContrastEnforced = false
        }
    }
}

fun ComponentActivity.applyInitialImmersiveEdgeToEdge() {
    val isSystemDark =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val isDark = when (Theme.nightMode.value) {
        NightMode.ON -> true
        NightMode.OFF -> false
        NightMode.AUTO, NightMode.MATERIAL_YOU -> isSystemDark
    }
    applyImmersiveEdgeToEdge(isDark)
}

@Composable
actual fun ApplyPlatformSystemAppearance(isDark: Boolean) {
    val context = LocalContext.current
    SideEffect {
        context.findComponentActivity()?.applyImmersiveEdgeToEdge(isDark)
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}
