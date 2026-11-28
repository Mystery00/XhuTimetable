package vip.mystery0.xhu.timetable.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.utils.md5

object Theme {
    val nightMode = MutableStateFlow(GlobalConfigStore.nightMode)
}

object XhuColor {
    val notThisWeekBackgroundColor = Color(0xFFE5E5E5)

    object Status {
        val beforeColor = Color(0xFF2E7D32)
        val inColor = Color(0xFFEF6C00)
        val afterColor = Color(0xFF9E9E9E)
    }
}

object CourseColorHelper {
    fun readableTextOn(backgroundColor: Color): Color {
        val l = backgroundColor.luminance()
        return if (l > 0.52f) Color(0xFF1C2024) else Color.White
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

    val random: Color
        get() = pool.random()

    fun safeGet(index: Int): Color = pool[index.mod(pool.size)]

    fun hash(text: String): Color {
        val md5Int = text.md5().substring(0, 2).toInt(16)
        return safeGet(md5Int)
    }
}

object XhuImages {
    val defaultBackgroundImageUri: String = "main_bg.png"
    val defaultProfileImage: String = "icon.svg"
}

data class BuiltinAvatar(
    val key: String,
    val label: String,
)

object ProfileImages {
    const val BUILTIN_PREFIX = "builtin:"
    const val FILE_PREFIX = "file:"

    val builtinAvatars: List<BuiltinAvatar> = listOf(
        BuiltinAvatar("boy1", "清爽学长"),
        BuiltinAvatar("boy2", "阳光运动"),
        BuiltinAvatar("boy3", "文艺书生"),
        BuiltinAvatar("boy4", "活力少年"),
        BuiltinAvatar("girl1", "温柔学姐"),
        BuiltinAvatar("girl2", "元气少女"),
        BuiltinAvatar("girl3", "知性清新"),
        BuiltinAvatar("girl4", "软萌可人"),
    )

    private val _customAvatarFlow = MutableStateFlow(GlobalConfigStore.customAvatar)
    val customAvatarFlow: StateFlow<String> = _customAvatarFlow

    fun updateCustomAvatar(value: String) {
        _customAvatarFlow.value = value
    }

    fun findBuiltin(key: String): BuiltinAvatar? = builtinAvatars.firstOrNull { it.key == key }
}
