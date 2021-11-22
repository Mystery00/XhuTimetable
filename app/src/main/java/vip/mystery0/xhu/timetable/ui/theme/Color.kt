package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.utils.md5

@Composable
private fun colorOf(pair: Pair<Color, Color>): Color =
    if (isDarkMode()) pair.first else pair.second

object XhuColor {
    val cardBackground: Color
        @Composable
        get() = colorOf(pair = Color(0xFF484848) to Color.White)
    val loginLabel: Color
        @Composable
        get() = Color(0xFFCCCCCC)
    val mainBarColorBackground: Color
        @Composable
        get() = colorOf(pair = Color(0xFF004D40) to Color.White)
    val iconChecked: Color
        @Composable
        get() = Color(0xFF2196F3)

    object Profile {
        val divider: Color
            @Composable
            get() = colorOf(pair = Color(0xFF363636) to Color(0xFFF0F0F0))
        val dividerSmall: Color
            @Composable
            get() = colorOf(pair = Color.White to Color(0xFF979797))
        val more: Color
            @Composable
            get() = colorOf(pair = Color(0xFF242424) to Color(0xFFEAEAEA))
    }

    object Common {
        val divider: Color
            @Composable
            get() = Color(0xFFDDDDDD)
        val grayBackground: Color
            @Composable
            get() = colorOf(pair = Color(0xFF212121) to Color(0xFFFAFAFA))
        val whiteBackground: Color
            @Composable
            get() = colorOf(pair = Color(0xFF242424) to Color.White)
        val grayText: Color
            @Composable
            get() = colorOf(pair = Color(0x8AFFFFFF) to Color(0x8A000000))
        val blackText: Color
            @Composable
            get() = colorOf(pair = Color.White to Color.Black)
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