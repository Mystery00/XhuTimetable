package vip.mystery0.xhu.timetable.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import vip.mystery0.xhu.timetable.config.store.Formatter
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

fun LocalDateTime.Companion.now(): LocalDateTime = Clock.System.now().asLocalDateTime()
fun LocalDate.Companion.now(): LocalDate = LocalDateTime.now().date
fun LocalTime.Companion.now(): LocalTime = LocalDateTime.now().time
val LocalDate.Companion.MIN: LocalDate
    get() = LocalDate(1900, 1, 1)

fun LocalDateTime.asInstant(): Instant = toInstant(Formatter.ZONE_CHINA)
fun Instant.asLocalDateTime(): LocalDateTime = toLocalDateTime(Formatter.ZONE_CHINA)

fun Duration.Companion.between(start: LocalDateTime, end: LocalDateTime): Duration =
    between(start.asInstant(), end.asInstant())

fun Duration.Companion.between(start: Instant, end: Instant): Duration {
    val startMills = start.toEpochMilliseconds()
    val endMills = end.toEpochMilliseconds()
    return (endMills - startMills).toDuration(DurationUnit.MILLISECONDS)
}

fun betweenDays(start: LocalDate, end: LocalDate): Long = end.toEpochDays() - start.toEpochDays()

fun List<Int>.formatWeekString(): String {
    if (isNullOrEmpty()) return ""
    val set = this.sorted()
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