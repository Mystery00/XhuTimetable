package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.maxkeppeler.sheets.color.models.MultipleColors
import vip.mystery0.xhu.timetable.utils.md5

object XhuColor {
    val cardBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainerLow

    val cardElevatedBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.surfaceContainer

    val notThisWeekBackgroundColor = Color(0xFFE5E5E5)

    object Status {
        val beforeColor = Color(0xFF2E7D32)
        val inColor = Color(0xFFEF6C00)
        val afterColor = Color(0xFF9E9E9E)

        @Composable
        fun beforeContainer(): Color =
            if (isDarkMode()) Color(0xFF1B3A24) else Color(0xFFE3F5E8)

        @Composable
        fun beforeContent(): Color =
            if (isDarkMode()) Color(0xFF81C784) else Color(0xFF1B5E20)

        @Composable
        fun inContainer(): Color =
            if (isDarkMode()) Color(0xFF42290A) else Color(0xFFFFF0DB)

        @Composable
        fun inContent(): Color =
            if (isDarkMode()) Color(0xFFFFB74D) else Color(0xFFE65100)

        @Composable
        fun afterContainer(): Color =
            MaterialTheme.colorScheme.surfaceContainerHighest

        @Composable
        fun afterContent(): Color =
            MaterialTheme.colorScheme.onSurfaceVariant
    }

    object Score {
        @Composable
        fun colorForScore(score: Number): Color {
            val s = score.toFloat()
            val dark = isDarkMode()
            return when {
                s < 60f -> if (dark) Color(0xFFFF8A80) else Color(0xFFD32F2F)
                s >= 90f -> if (dark) Color(0xFF69F0AE) else Color(0xFF1B7F46)
                s >= 75f -> if (dark) Color(0xFF80CBC4) else Color(0xFF00796B)
                else -> if (dark) Color(0xFFFFCC80) else Color(0xFFE65100)
            }
        }

        @Composable
        fun containerForScore(score: Number): Color {
            val s = score.toFloat()
            val dark = isDarkMode()
            return when {
                s < 60f -> if (dark) Color(0xFF4A1B1B) else Color(0xFFFFEBEE)
                s >= 90f -> if (dark) Color(0xFF153826) else Color(0xFFE3F6EB)
                s >= 75f -> if (dark) Color(0xFF133330) else Color(0xFFE0F2F1)
                else -> if (dark) Color(0xFF3E2712) else Color(0xFFFFF3E0)
            }
        }
    }
}

/**
 * 根据背景色和明暗模式智能生成高对比度的前景文字色与柔和容器色
 */
object CourseColorHelper {
    /**
     * 用于课程表实色格子：在浅色/深色模式下微调饱和度，并自动计算高对比度文字色
     */
    @Composable
    fun weekCellContainer(rawColor: Color, alpha: Float): Color {
        val dark = isDarkMode()
        val tuned = if (dark) {
            lerp(rawColor, Color(0xFF1A1E24), 0.24f)
        } else {
            // 略微柔和极高饱和度，同时保持清晰区分
            lerp(rawColor, Color.White, 0.06f)
        }
        return tuned.copy(alpha = alpha.coerceIn(0.25f, 1f))
    }

    fun readableTextOn(backgroundColor: Color): Color {
        val l = backgroundColor.luminance()
        return if (l > 0.52f) Color(0xFF1C2024) else Color.White
    }

    /**
     * 用于亮色/深色背景上的彩色强调文字（如倒计时数字、标题高亮），保证在深色卡片上清晰可读
     */
    @Composable
    fun accentText(rawColor: Color): Color {
        val dark = isDarkMode()
        return if (dark) {
            if (rawColor.luminance() < 0.35f) lerp(rawColor, Color.White, 0.48f) else rawColor
        } else {
            if (rawColor.luminance() > 0.55f) lerp(rawColor, Color.Black, 0.35f) else rawColor
        }
    }

    /**
     * 用于日历/详情卡片的柔和容器色（淡色底 + 清晰深/浅文字）
     */
    @Composable
    fun softContainer(rawColor: Color, alpha: Float = 0.94f): Color {
        val dark = isDarkMode()
        val base = if (dark) {
            lerp(Color(0xFF1E2228), rawColor, 0.22f)
        } else {
            lerp(Color.White, rawColor, 0.14f)
        }
        return base.copy(alpha = alpha)
    }
}

object ColorPool {
    private val pool = listOf(
        Color(0xFFE55353),
        Color(0xFFD84B84),
        Color(0xFF8E55B8),
        Color(0xFF6B5CE7),
        Color(0xFF4A69BD),
        Color(0xFF2E86DE),
        Color(0xFF1B9CFC),
        Color(0xFF00A8B5),
        Color(0xFF16A085),
        Color(0xFF2ECC71),
        Color(0xFF6AB04C),
        Color(0xFFB5A624),
        Color(0xFFE59A23),
        Color(0xFFEB7D2A),
        Color(0xFFE66742),
        Color(0xFF8D6E63),
        Color(0xFFC0396B),
        Color(0xFF2D8659),
    )

    val allColors: List<Color>
        get() = pool

    val templateColors: MultipleColors = MultipleColors.ColorsInt(pool.map { it.toArgb() })

    val random: Color
        get() = pool.random()

    fun safeGet(index: Int): Color = pool[index.mod(pool.size)]

    fun hash(text: String): Color {
        val md5Int = text.md5().substring(0, 2).toInt(16)
        return safeGet(md5Int)
    }
}
