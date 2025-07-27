package vip.mystery0.xhu.timetable.config.datetime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("xhu.kotlin.time.Instant", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeLong(value.toEpochMilliseconds())

    override fun deserialize(decoder: Decoder): Instant =
        Instant.fromEpochMilliseconds(decoder.decodeLong())
}