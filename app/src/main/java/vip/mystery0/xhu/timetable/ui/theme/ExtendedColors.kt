package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val surfaceContainer: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceContainer = Color.Unspecified,
    )
}

object ExtendedTheme {
    val colorScheme: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}