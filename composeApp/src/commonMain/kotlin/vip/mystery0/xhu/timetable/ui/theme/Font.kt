package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em

object XhuFonts {
    val DEFAULT: FontFamily = FontFamily.Default

    fun Typography.globalSet(fontFamily: FontFamily): Typography =
        copy(
            displayLarge = this.displayLarge.globalSet(fontFamily, 1.2f),
            displayMedium = this.displayMedium.globalSet(fontFamily, 1.2f),
            displaySmall = this.displaySmall.globalSet(fontFamily, 1.22f),
            headlineLarge = this.headlineLarge.globalSet(fontFamily, 1.25f),
            headlineMedium = this.headlineMedium.globalSet(fontFamily, 1.25f),
            headlineSmall = this.headlineSmall.globalSet(fontFamily, 1.28f),
            titleLarge = this.titleLarge.globalSet(fontFamily, 1.3f),
            titleMedium = this.titleMedium.globalSet(fontFamily, 1.32f),
            titleSmall = this.titleSmall.globalSet(fontFamily, 1.32f),
            bodyLarge = this.bodyLarge.globalSet(fontFamily, 1.42f),
            bodyMedium = this.bodyMedium.globalSet(fontFamily, 1.42f),
            bodySmall = this.bodySmall.globalSet(fontFamily, 1.38f),
            labelLarge = this.labelLarge.globalSet(fontFamily, 1.28f),
            labelMedium = this.labelMedium.globalSet(fontFamily, 1.28f),
            labelSmall = this.labelSmall.globalSet(fontFamily, 1.25f),
        )

    private fun TextStyle.globalSet(fontFamily: FontFamily, lineHeightEm: Float) =
        copy(
            lineHeight = lineHeightEm.em,
            fontFamily = fontFamily,
        )
}
