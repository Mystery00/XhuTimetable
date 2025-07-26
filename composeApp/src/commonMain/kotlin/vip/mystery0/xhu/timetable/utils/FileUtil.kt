package vip.mystery0.xhu.timetable.utils

import kotlin.math.pow
import kotlin.math.roundToLong

fun Long.formatFileSize(decimalNum: Int = 2): String {
    if (this <= 0L)
        return "0B"
    val formatString = StringBuilder()
    formatString.append("#.")
    val digits = if (decimalNum <= 0)
        1
    else
        decimalNum
    return when {
        this < 1024 -> formatDecimal(toDouble(), digits) + "B"
        this < 1024 * 1024 -> formatDecimal(this / 1024.0, digits) + "KB"
        this < 1024 * 1024 * 1024 -> formatDecimal(this / 1024.0 / 1024.0, digits) + "MB"
        else -> formatDecimal(this / 1024.0 / 1024.0 / 1024.0, digits) + "GB"
    }
}

fun Double.formatWithDecimal(decimalNum: Int = 2): String = formatDecimal(this, decimalNum)

fun formatDecimal(value: Double, digits: Int = 1): String {
    val multiplier = 10.0.pow(digits)
    val rounded = (value * multiplier).roundToLong() / multiplier
    return rounded.toString()
}