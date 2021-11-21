package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.utils.md5

object XhuColor {
    val cardBackground: Color
        @Composable
        get() = if (isSystemInDarkTheme()) Color(0xFF484848) else Color.White
    val loginLabel: Color
        @Composable
        get() = colorResource(id = R.color.login_color_label)
    val mainBarColorBackground: Color
        @Composable
        get() = colorResource(id = R.color.main_bar_color_background)
    val iconChecked: Color
        @Composable
        get() = colorResource(id = R.color.iconCheckedColor)

    object Common {
        val divider: Color
            @Composable
            get() = colorResource(id = R.color.colorDivider)
        val grayBackground: Color
            @Composable
            get() = colorResource(id = R.color.colorGrayBackground)
        val whiteBackground: Color
            @Composable
            get() = colorResource(id = R.color.colorWhiteBackground)
        val grayText: Color
            @Composable
            get() = colorResource(id = R.color.colorGrayText)
        val blackText: Color
            @Composable
            get() = colorResource(id = R.color.colorBlackText)
        val nullDataColor: Color
            @Composable
            get() = colorResource(id = R.color.textColorNullDataView)
    }

    object Status {
        val beforeColor = Color(0xFF4CAF50)
        val beforeBackgroundColor = Color(0xFFC8E6C9)
        val inColor = Color(0xFFFF9800)
        val inBackgroundColor = Color(0xFFFFE0B2)
        val afterColor = Color(0xFFC6C6C6)
        val afterBackgroundColor = Color(0xFFF5F5F5)
    }
}

object ColorPool {
    private val pool = listOf(
        Color(0xFF7c4dff),
        Color(0xFF536dfe),
        Color(0xFF03a9f4),
        Color(0xFFff3d00),
        Color(0xFFff8f00),
        Color(0xFFfec106),
        Color(0xFF8bc34a),
        Color(0xFF12c700),
        Color(0xFFff3a6b),
    )

    val random: Color
        get() = pool.random()

    fun hash(text: String): Color {
        val md5Int = text.md5().substring(0, 2).toInt(16)
        return pool[md5Int % pool.size]
    }
}