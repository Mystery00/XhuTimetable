package com.maxkeppeler.sheets.clock.views

import com.maxkeppeler.sheets.clock.utils.FormatStyle
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.char

internal actual fun getTimeFormatter(style: FormatStyle): DateTimeFormat<LocalTime> = LocalTime.Format {
    hour()
    char(':')
    minute()
    if (style != FormatStyle.SHORT) {
        char(':')
        second()
    }
}
