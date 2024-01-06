package vip.mystery0.xhu.timetable.utils

import vip.mystery0.xhu.timetable.config.store.Formatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TreeSet

fun LocalDateTime.asInstant(): Instant = atZone(Formatter.ZONE_CHINA).toInstant()
fun Instant.asLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, Formatter.ZONE_CHINA)

fun Instant.formatChinaDateTime(): String = asLocalDateTime().formatChinaDateTime()
fun LocalDateTime.formatChinaDateTime(): String =
    format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒"))

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH时mm分")
val thingDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
val calendarTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val calendarMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")

fun List<Int>.formatWeekString(): String {
    if (isNullOrEmpty()) return ""
    val set = TreeSet(this)
    var start = -1
    var temp = -1
    val stringBuilder = StringBuilder()
    set.forEach {
        if (it == temp + 1) {
            //连续数字
            temp = it
            return@forEach
        }
        //非连续数字
        if (start == -1) {
            //第一次循环
            start = it
            temp = it
            return@forEach
        }
        //非第一次循环
        stringBuilder.append(start)
        if (temp != start) {
            stringBuilder.append("-").append(temp)
        }
        stringBuilder.append("、")
        start = it
        temp = it
    }
    stringBuilder.append(start)
    if (temp != start) {
        stringBuilder.append("-").append(temp)
    }
    return stringBuilder.append("周").toString()
}