@file:Suppress("UNUSED")
package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.Flushable
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * @return a new serializer capable of writing primitives, primitive arrays, and strings to Kanary format
 */
fun OutputStream.serializer() = PrimitiveSerializer(this)

/**
 * @return a new serializer capable of writing primitives, primitive arrays, strings and
 * instances of any ype with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: ProtocolSet) = Serializer(this, protocols)

/**
 * Writes serialized data to a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Because no protocols are defined, no instances of any reference types may be written.
 * Calling [close] also closes the underlying stream.
 */
open class PrimitiveSerializer internal constructor(@PublishedApi internal val stream: OutputStream) : Closeable, Flushable {
    fun write(cond: Boolean) {
        BOOLEAN.mark(stream)
        stream.write(if (cond) 1 else 0)
    }

    fun write(b: Byte) {
        BYTE.mark(stream)
        stream.write(b.toInt())
    }

    fun write(c: Char) {
        CHAR.mark(stream)
        writeBytesNoMark(Char.SIZE_BYTES) { putChar(c) }
    }

    fun write(n: Short) {
        SHORT.mark(stream)
        writeBytesNoMark(Short.SIZE_BYTES) { putShort(n) }
    }

    fun write(n: Int) {
        INT.mark(stream)
        writeNoMark(n)
    }

    fun write(n: Long) {
        LONG.mark(stream)
        writeBytesNoMark(Long.SIZE_BYTES) { putLong(n) }
    }

    fun write(fp: Float) {
        FLOAT.mark(stream)
        writeBytesNoMark(Float.SIZE_BYTES) { putFloat(fp) }
    }

    fun write(fp: Double) {
        DOUBLE.mark(stream)
        writeBytesNoMark(Double.SIZE_BYTES) { putDouble(fp) }
    }

    fun write(condArr: BooleanArray) {
        BOOLEAN_ARRAY.mark(stream)
        condArr.forEach { stream.write(if (it) 1 else 0) }
    }

    fun write(bArr: ByteArray) {
        BYTE_ARRAY.mark(stream)
        writeBytesNoMark(Int.SIZE_BYTES) { putInt(bArr.size) }
        stream.write(bArr)
    }

    fun write(cArr: CharArray) {
        CHAR_ARRAY.mark(stream)
        writeArrayNoMark(Char.SIZE_BYTES, cArr.size) { cArr.forEach { putChar(it) } }
    }

    fun write(nArr: ShortArray) {
        SHORT_ARRAY.mark(stream)
        writeArrayNoMark(Short.SIZE_BYTES, nArr.size) { nArr.forEach { putShort(it) } }
    }

    fun write(nArr: IntArray) {
        INT_ARRAY.mark(stream)
        writeArrayNoMark(Int.SIZE_BYTES, nArr.size) { nArr.forEach { putInt(it) } }
    }

    fun write(nArr: LongArray) {
        LONG_ARRAY.mark(stream)
        writeArrayNoMark(Long.SIZE_BYTES, nArr.size) { nArr.forEach { putLong(it) } }
    }

    fun write(nArr: FloatArray) {
        FLOAT_ARRAY.mark(stream)
        writeArrayNoMark(Float.SIZE_BYTES, nArr.size) { nArr.forEach { putFloat(it) } }
    }

    fun write(nArr: DoubleArray) {
        DOUBLE_ARRAY.mark(stream)
        writeArrayNoMark(Double.SIZE_BYTES, nArr.size) { nArr.forEach { putDouble(it) } }
    }

    fun write(s: String) {  // marker, size, char...
        STRING.mark(stream)
        writeNoMark(s)
    }

    override fun flush() = stream.flush()
    override fun close() = stream.close()

    @PublishedApi
    internal fun writeNoMark(n: Int) = writeBytesNoMark(Int.SIZE_BYTES) { putInt(n) }

    @PublishedApi
    internal fun writeNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeNoMark(bytes.size)
        stream.write(bytes)
    }

    @PublishedApi
    internal inline fun writeBytesNoMark(count: Int, write: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(count)
        write(buffer)
        stream.write(buffer.array())
    }

    // marker, size, member...
    private inline fun writeArrayNoMark(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1*Int.SIZE_BYTES + size*memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }
}

/**
 * A [PrimitiveSerializer] that allows the writing of objects whose types have a defined protocol.
 */
class Serializer internal constructor(
    stream: OutputStream,
    private val protocols: ProtocolSet
) : PrimitiveSerializer(stream) {
    /**
     * Writes all members in array according to the protocol of each instance.
     * @throws MissingProtocolException the type of any member of [nullablesArr] is not null, and
     * is not a top-level class or does not have a defined protocol
     */
    inline fun <T, reified N : T & Any> writeAllNullables(nullablesArr: Array<out T>) {  // marker, type, size, (marker, member)...
        NULLABLE_ARRAY.mark(stream)
        writeNoMark(nullablesArr.size)
        nullablesArr.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
    }

    /**
     * Writes all members in array according to the protocol of each instance.
     * @throws MissingProtocolException the type of any member of [objArr]
     * is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(objArr: Array<out T>) {  // marker, type, size, (marker, member)...
        OBJECT_ARRAY.mark(stream)
        writeNoMark(objArr.size)
        objArr.forEach { write(it) }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * @throws MissingProtocolException any member of [nullablesList] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> writeAllNullables(nullablesList: List<T>) {   // marker, size, (marker, member)...
        NULLABLE_LIST.mark(stream)
        writeNoMark(nullablesList.size)
        nullablesList.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * @throws MissingProtocolException any member of [list] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(list: List<T>) { // marker, size, (marker, member)...
        LIST.mark(stream)
        writeNoMark(list.size)
        list.forEach { write(it) }
    }

    /**
     * Writes all members in the iterable object according the protocol of each instance as a list.
     * The caller must ensure that the object has a finite number of members.
     * @throws MissingProtocolException any member of [nullablesIter] is not null, and
     * its type is not top-level class or does not have a defined protocol
     */
    inline fun <reified T> writeAllNullables(nullablesIter: Iterable<T>) {  // begin, (marker, member)..., end
        NULLABLE_BEGIN.mark(stream)
        nullablesIter.forEach {
            if (it == null) {
                NULL.mark(stream)
                return@forEach
            }
            write(it)
        }
        SENTINEL.mark(stream)
    }

    /**
     * Writes all members in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of members.
     * @throws MissingProtocolException any member of [iter] is not a top-level class or does not have a defined protocol
     */
    inline fun <reified T : Any> writeAll(iter: Iterable<T>) { // begin, (marker, member)..., end
        ITERABLE_BEGIN.mark(stream)
        iter.forEach { write(it) }
        SENTINEL.mark(stream)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     * @throws MissingProtocolException if [nullable] is not null, and
     * its type is not a top-level class or does not have a defined protocol
     */
    fun writeNullable(nullable: Any?) {
        if (nullable == null) {
            NULL.mark(stream)
            return
        }
        write(nullable)
    }

    /**
     * Writes the object according to the protocol of its type.
     * If the type of the object does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     * @throws MissingProtocolException the type of [obj] is not a top-level class or does not have a defined protocol
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> write(obj: T) {
        OBJECT.mark(stream)
        val classRef = obj::class
        var className = protocolNameOf(classRef)
        var protocol: Protocol<in T>? = protocols.resolve(className)
        if (protocol == null) {
            val cache = protocols.superclassProtocolCache
            val cachedProtocol = cache[className]
                ?: classRef.findWriteProtocol()?.also { cache[className] = it }
                ?: throw MissingProtocolException("No binary I/O protocols found for any superclass of type '$className'")
            className = cachedProtocol.first
            protocol = cachedProtocol.second as Protocol<in T>
        }
        writeNoMark(className)
        protocol.write(this, obj)
    }

    private fun <T : Any> KClass<out T>.findWriteProtocol(): Pair<String,Protocol<in T>>? {
        for (superclass in superclasses) {
            val superclassName = superclass.qualifiedName!! // Will always be valid class/interface
            val protocol = protocols.resolve<T>(superclassName)
            if (protocol != null) {
                return superclassName to protocol
            }
        }
        for (superclass in superclasses) {
            val recursion = superclass.findWriteProtocol()
            if (recursion != null) {
                return recursion
            }
        }
        return null
    }
}