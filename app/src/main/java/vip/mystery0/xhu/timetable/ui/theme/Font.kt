package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore

object XhuFonts {
    val DEFAULT: FontFamily = custom ?: FontFamily.Default

    val custom: FontFamily?
        get() {
            val fontFile = GlobalConfigStore.customFontFile ?: return null
            return FontFamily(Font(fontFile))
        }

    fun Typography.globalSet(fontFamily: FontFamily): Typography =
        copy(
            displayLarge = this.displayLarge.globalSet(fontFamily),
            displayMedium = this.displayMedium.globalSet(fontFamily),
            displaySmall = this.displaySmall.globalSet(fontFamily),
            headlineLarge = this.headlineLarge.globalSet(fontFamily),
            headlineMedium = this.headlineMedium.globalSet(fontFamily),
            headlineSmall = this.headlineSmall.globalSet(fontFamily),
            titleLarge = this.titleLarge.globalSet(fontFamily),
            titleMedium = this.titleMedium.globalSet(fontFamily),
            titleSmall = this.titleSmall.globalSet(fontFamily),
            bodyLarge = this.bodyLarge.globalSet(fontFamily),
            bodyMedium = this.bodyMedium.globalSet(fontFamily),
            bodySmall = this.bodySmall.globalSet(fontFamily),
            labelLarge = this.labelLarge.globalSet(fontFamily),
            labelMedium = this.labelMedium.globalSet(fontFamily),
            labelSmall = this.labelSmall.globalSet(fontFamily),
        )

    private fun TextStyle.globalSet(fontFamily: FontFamily) =
        copy(
            lineHeight = 1.2.em,
            fontFamily = fontFamily,
        )
}