package vip.mystery0.xhu.timetable.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TreeSet

private val chinaZone: ZoneId = ZoneId.of("Asia/Shanghai")
fun LocalDateTime.asInstant(): Instant = atZone(chinaZone).toInstant()
fun Instant.asLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, chinaZone)

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
val dateWithWeekFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
val timeFormatter = DateTimeFormatter.ofPattern("HH时mm分")
val chinaDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒")
val thingDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")

val enDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val enTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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