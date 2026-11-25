package com.maxkeppeler.sheets.date_time.utils

internal actual fun getLocalizedPattern(isDate: Boolean, formatStyle: FormatStyle): String = when {
    isDate -> "yyyy-MM-dd"
    formatStyle == FormatStyle.SHORT -> "HH:mm"
    else -> "HH:mm:ss"
}
