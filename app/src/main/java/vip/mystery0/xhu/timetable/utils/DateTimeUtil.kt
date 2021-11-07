package vip.mystery0.xhu.timetable.utils

import java.time.format.DateTimeFormatter

val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
val timeFormatter = DateTimeFormatter.ofPattern("HH时mm分")
val chinaDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒")