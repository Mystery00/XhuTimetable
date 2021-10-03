package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.utils.md5

object XhuColor {
    val loginLabel: Color
        @Composable
        get() = colorResource(id = R.color.login_color_label)
    val loginText: Color
        @Composable
        get() = colorResource(id = R.color.login_color_text)
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