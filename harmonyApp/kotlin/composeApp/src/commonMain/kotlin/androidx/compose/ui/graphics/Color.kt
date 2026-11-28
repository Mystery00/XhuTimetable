package androidx.compose.ui.graphics

import kotlin.jvm.JvmInline
import kotlin.math.pow

@JvmInline
value class Color(val value: ULong) {
    private val argb: Int
        get() = (value shr 32).toInt()

    val alpha: Float
        get() = ((argb ushr 24) and 0xFF) / 255f

    val red: Float
        get() = ((argb ushr 16) and 0xFF) / 255f

    val green: Float
        get() = ((argb ushr 8) and 0xFF) / 255f

    val blue: Float
        get() = (argb and 0xFF) / 255f

    fun copy(
        alpha: Float = this.alpha,
        red: Float = this.red,
        green: Float = this.green,
        blue: Float = this.blue,
    ): Color = Color(
        red = (red.coerceIn(0f, 1f) * 255f + 0.5f).toInt(),
        green = (green.coerceIn(0f, 1f) * 255f + 0.5f).toInt(),
        blue = (blue.coerceIn(0f, 1f) * 255f + 0.5f).toInt(),
        alpha = (alpha.coerceIn(0f, 1f) * 255f + 0.5f).toInt(),
    )

    companion object {
        val Black = Color(0xFF000000)
        val DarkGray = Color(0xFF444444)
        val Gray = Color(0xFF888888)
        val LightGray = Color(0xFFCCCCCC)
        val White = Color(0xFFFFFFFF)
        val Transparent = Color(0x00000000)
        val Unspecified = Color(0x00000000_00000010UL)
    }
}

fun Color(color: Long): Color = Color((color.toInt().toUInt().toULong()) shl 32)

fun Color(color: Int): Color = Color((color.toUInt().toULong()) shl 32)

fun Color(red: Int, green: Int, blue: Int, alpha: Int = 0xFF): Color {
    val argb = ((alpha and 0xFF) shl 24) or
        ((red and 0xFF) shl 16) or
        ((green and 0xFF) shl 8) or
        (blue and 0xFF)
    return Color(argb)
}

fun Color.toArgb(): Int = (value shr 32).toInt()

private fun srgbToLinear(channel: Float): Double {
    val c = channel.toDouble()
    return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
}

fun Color.luminance(): Float {
    val r = srgbToLinear(red)
    val g = srgbToLinear(green)
    val b = srgbToLinear(blue)
    return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat().coerceIn(0f, 1f)
}
