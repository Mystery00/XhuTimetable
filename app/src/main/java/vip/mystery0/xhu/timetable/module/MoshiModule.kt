package vip.mystery0.xhu.timetable.module

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun Moshi.Builder.registerAdapter(): Moshi.Builder {
    add(LocalDate::class.java, LocalDateAdapter())
    add(LocalTime::class.java, LocalTimeAdapter())
    add(Instant::class.java, InstantAdapter())
    addLast(KotlinJsonAdapterFactory())
    return this
}

class LocalDateAdapter : JsonAdapter<LocalDate>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun fromJson(reader: JsonReader): LocalDate? {
        val value = reader.nextString()
        if (value.isNullOrBlank()) {
            return null
        }
        return LocalDate.parse(value, formatter)
    }

    override fun toJson(writer: JsonWriter, value: LocalDate?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.value(formatter.format(value))
    }
}

class LocalTimeAdapter : JsonAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    override fun fromJson(reader: JsonReader): LocalTime? {
        val value = reader.nextString()
        if (value.isNullOrBlank()) {
            return null
        }
        return LocalTime.parse(value, formatter)
    }

    override fun toJson(writer: JsonWriter, value: LocalTime?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.value(formatter.format(value))
    }
}

class InstantAdapter : JsonAdapter<Instant>() {
    override fun fromJson(reader: JsonReader): Instant? {
        val value = reader.nextLong()
        return Instant.ofEpochMilli(value)
    }

    override fun toJson(writer: JsonWriter, value: Instant?) {
        if (value == null) {
            writer.nullValue()
            return
        }
        writer.value(value.toEpochMilli())
    }
}

fun betweenDays(start: LocalDate, end: LocalDate): Int =
    end.toEpochDay().toInt() - start.toEpochDay().toInt()