package vip.mystery0.xhu.timetable.utils

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

fun Color.toHexString(): String = toArgbHexManual(255F, red * 255F, green * 255F, blue * 255F)

fun String.parseColorHexString(): Color {
    val cleanHex = removePrefix("#")
    if (this.isBlank()) {
        return ColorPool.random
    }
    val data = if (cleanHex.length == 8) cleanHex else "FF$cleanHex"
    val (a, r, g, b) = data.chunked(2).map { it.hexToInt() }
    return Color(r, g, b, a)
}

/**
 * 将单个整数值转换为两位大写十六进制字符串。
 */
private fun Int.toHex(): String = toHexString(HexFormat {
    upperCase = true
    number {
        removeLeadingZeros = true
        minLength = 2
    }
})

/**
 * 将 ARGB 整数值转换为 #AARRGGBB 格式的十六进制字符串。
 * (手动实现版本)
 */
private fun toArgbHexManual(a: Float, r: Float, g: Float, b: Float): String {
    require(a in 0F..255F && r in 0F..255F && g in 0F..255F && b in 0F..255F) {
        "ARGB values must be between 0 and 255."
    }
    return "#${a.toInt().toHex()}${r.toInt().toHex()}${g.toInt().toHex()}${b.toInt().toHex()}"
}