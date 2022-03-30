package xyz.mastriel.hostiletakeover.serializers

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias MutableBoolean = @Serializable(with = MutableBooleanSerializer::class) MutableState<Boolean>
typealias MutableString = @Serializable(with = MutableStringSerializer::class) MutableState<String>
typealias MutableLong = @Serializable(with = MutableLongSerializer::class) MutableState<Long>

interface MutableStateSerializer<T> : KSerializer<MutableState<T>>

object MutableBooleanSerializer : MutableStateSerializer<Boolean> {
    override fun deserialize(decoder: Decoder): MutableState<Boolean> {
        return mutableStateOf(decoder.decodeBoolean())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Boolean", PrimitiveKind.BOOLEAN)

    override fun serialize(encoder: Encoder, value: MutableState<Boolean>) {
        encoder.encodeBoolean(value.value)
    }

}

object MutableStringSerializer : MutableStateSerializer<String> {
    override fun deserialize(decoder: Decoder): MutableState<String> {
        return mutableStateOf(decoder.decodeString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("String", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MutableState<String>) {
        encoder.encodeString(value.value)
    }

}

object MutableLongSerializer : MutableStateSerializer<Long> {
    override fun deserialize(decoder: Decoder): MutableState<Long> {
        return mutableStateOf(decoder.decodeString().toLong())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Long", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MutableState<Long>) {
        encoder.encodeString(value.value.toString())
    }

}