package vip.mystery0.xhu.timetable.config.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlin.time.Instant

typealias XhuInstant = @Serializable(InstantSerializer::class) Instant
typealias XhuLocalDateTime = @Serializable(LocalDateTimeSerializer::class) LocalDateTime
typealias XhuLocalDate = @Serializable(LocalDateSerializer::class) LocalDate
typealias XhuLocalTime = @Serializable(LocalTimeSerializer::class) LocalTime