package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import vip.mystery0.xhu.timetable.config.GlobalConfig

object XhuFonts {
    @OptIn(ExperimentalTextApi::class)
    val custom: FontFamily?
        get() {
            val fontFile = GlobalConfig.customFontFile ?: return null
            return FontFamily(Font(fontFile))
        }
}