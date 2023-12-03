package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.maxkeppeler.sheets.color.models.MultipleColors
import vip.mystery0.xhu.timetable.utils.md5

@Composable
private fun colorOf(pair: Pair<Color, Color>): Color =
    if (isDarkMode()) pair.first else pair.second

object XhuColor {
    val cardBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val surfaceContainer: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    val loginLabel: Color
        @Composable
        get() = Color(0xFFCCCCCC)
    val customCourseWeekColorBackground: Color
        get() = Color(0xFFE5E5E5)
    val notThisWeekBackgroundColor: Color
        get() = Color(0xFFe5e5e5)

    object Common {
        val divider: Color
            @Composable
            get() = Color(0xFFDDDDDD)
        val grayText: Color
            @Composable
            get() = colorOf(pair = Color(0x8AFFFFFF) to Color(0x8A000000))
    }

    object Status {
        val beforeColor = Color(0xFF4CAF50)
        val inColor = Color(0xFFFF9800)
        val afterColor = Color(0xFFC6C6C6)
    }
}

object ColorPool {
    private val pool = listOf(
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFFF5722),
        Color(0xFF795548),
        Color(0xFF9E9E9E),
        Color(0xFF607D8B),
    )

    val templateColors: MultipleColors = MultipleColors.ColorsInt(pool.map { it.toArgb() })

    val random: Color
        get() = pool.random()

    fun safeGet(index: Int): Color = pool[index % pool.size]

    fun hash(text: String): Color {
        val md5Int = text.md5().substring(0, 2).toInt(16)
        return safeGet(md5Int)
    }
}