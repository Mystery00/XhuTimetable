package vip.mystery0.xhu.timetable.config.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = LocalDate.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("xhu.kotlinx.datetime.LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.format(formatter))

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString(), formatter)
}