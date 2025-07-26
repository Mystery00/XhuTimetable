package vip.mystery0.xhu.timetable.config.datetime

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val delegateSerializer = InstantSerializer
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("xhu.kotlinx.datetime.LocalDateTime", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val instant = value.asInstant()
        encoder.encodeSerializableValue(delegateSerializer, instant)
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val instant = decoder.decodeSerializableValue(delegateSerializer)
        return instant.asLocalDateTime()
    }
}