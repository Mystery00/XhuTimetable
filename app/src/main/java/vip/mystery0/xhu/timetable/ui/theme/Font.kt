package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore

object XhuFonts {
    val DEFAULT: FontFamily = custom ?: FontFamily.Default

    val custom: FontFamily?
        get() {
            val fontFile = GlobalConfigStore.customFontFile ?: return null
            return FontFamily(Font(fontFile))
        }
}