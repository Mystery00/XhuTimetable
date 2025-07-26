package vip.mystery0.xhu.timetable.utils

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber

val dateFormatter = LocalDate.Format {
    year()
    char('年')
    monthNumber()
    char('月')
    day()
    char('日')
}
val timeFormatter = LocalTime.Format {
    hour()
    char('时')
    minute()
    char('分')
}
val thingDateTimeFormatter = LocalDateTime.Format {
    year()
    char('/')
    monthNumber()
    char('/')
    day()
    char(' ')
    hour()
    char(':')
    minute()
}
val thingDateFormatter = LocalDate.Format {
    year()
    char('/')
    monthNumber()
    char('/')
    day()
}
val thingTimeFormatter = LocalTime.Format {
    hour()
    char(':')
    minute()
}
val calendarTimeFormatter = LocalTime.Format {
    hour()
    char(':')
    minute()
}
val calendarMonthFormatter = LocalDate.Format {
    year()
    char('年')
    monthNumber()
    char('月')
}
val chinaDateTime = LocalDateTime.Format {
    year()
    char('年')
    monthNumber()
    char('月')
    day()
    char('日')
    char(' ')
    hour()
    char('时')
    minute()
    char('分')
    second()
    char('秒')
}

fun DayOfWeek.formatChina(): String =
    when (this.isoDayNumber) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> ""
    }

fun DayOfWeek.formatWeekString(): String =
    when (this.isoDayNumber) {
        1 -> "星期一"
        2 -> "星期二"
        3 -> "星期三"
        4 -> "星期四"
        5 -> "星期五"
        6 -> "星期六"
        7 -> "星期日"
        else -> ""
    }