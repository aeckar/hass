package io.github.aeckar.hass.io

import io.github.aeckar.hass.reflect.Type
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.reflect.jvm.jvmName

/**
 * Encodes type-specific data to the underlying stream in Hass format.
 */
internal class Encoder(override val stream: OutputStream) : DataStream() {
    // ------------------------------ primitive write operations ------------------------------

    fun encodeBoolean(cond: Boolean) = stream.write(if (cond) 1 else 0)
    fun encodeByte(b: Byte) = stream.write(b.toInt())
    fun encodeChar(n: Char) = encodePrimitive(Char.SIZE_BYTES) { putChar(n) }
    fun encodeShort(n: Short) = encodePrimitive(Short.SIZE_BYTES) { putShort(n) }
    fun encodeInt(n: Int) = encodePrimitive(Int.SIZE_BYTES) { putInt(n) }
    fun encodeLong(n: Long) = encodePrimitive(Long.SIZE_BYTES) { putLong(n) }
    fun encodeFloat(n: Float) = encodePrimitive(Float.SIZE_BYTES) { putFloat(n) }
    fun encodeDouble(n: Double) = encodePrimitive(Double.SIZE_BYTES) { putDouble(n) }

    // ------------------------------ object write operations ------------------------------

    fun encodeTypeFlag(flag: TypeFlag) = stream.write(flag.ordinal)
    fun encodeSerializable(obj: Any) = ObjectOutputStream(stream).writeObject(obj)
    fun encodeType(kClass: Type) = encodeString(kClass.jvmName)

    fun encodeString(s: String) {
        val byteArray = s.toByteArray(Charsets.UTF_8)
        encodeInt(byteArray.size)
        stream.write(byteArray)
    }

    // ------------------------------------------------------------------------
    
    private inline fun encodePrimitive(sizeBytes: Int, put: ByteBuffer.() -> Unit) {
        val byteBuffer = buffer.asByteBuffer()
        byteBuffer.apply(put)
        stream.write(byteBuffer.array(), 0, sizeBytes)
    }
}