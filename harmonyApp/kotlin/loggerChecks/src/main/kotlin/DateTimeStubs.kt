package vip.mystery0.xhu.timetable.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.asLocalDateTime() = toLocalDateTime(TimeZone.currentSystemDefault())
