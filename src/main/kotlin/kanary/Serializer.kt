@file:Suppress("UNUSED")
package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.Flushable
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * See [ProtocolSet] for the types with pre-defined binary I/O protocols.
 * @return a new serializer capable of writing primitives, primitive arrays,
 * and instances of any type with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: ProtocolSet = ProtocolSet.DEFAULT) = Serializer(this, protocols)

/**
 * Writes serialized data to a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Because no protocols are defined, no instances of any reference types may be written.
 * Calling [close] also closes the underlying stream.
 */
class Serializer internal constructor(
    @PublishedApi internal val stream: OutputStream,
    private val protocols: ProtocolSet
) : Closeable by stream, Flushable by stream {
    fun writeBoolean(cond: Boolean) {
        BOOLEAN.mark(stream)
        stream.write(if (cond) 1 else 0)
    }

    fun writeByte(b: Byte) {
        BYTE.mark(stream)
        stream.write(b.toInt())
    }

    fun writeChar(c: Char) {
        CHAR.mark(stream)
        writeBytesNoMark(Char.SIZE_BYTES) { putChar(c) }
    }

    fun writeShort(n: Short) {
        SHORT.mark(stream)
        writeBytesNoMark(Short.SIZE_BYTES) { putShort(n) }
    }

    fun writeInt(n: Int) {
        INT.mark(stream)
        writeIntNoMark(n)
    }

    fun writeLong(n: Long) {
        LONG.mark(stream)
        writeBytesNoMark(Long.SIZE_BYTES) { putLong(n) }
    }

    fun writeFloat(fp: Float) {
        FLOAT.mark(stream)
        writeBytesNoMark(Float.SIZE_BYTES) { putFloat(fp) }
    }

    fun writeDouble(fp: Double) {
        DOUBLE.mark(stream)
        writeBytesNoMark(Double.SIZE_BYTES) { putDouble(fp) }
    }

    /**
     * Writes all members in array according to the protocol of each instance.
     * Avoids null check for members, unlike generic `write`.
     * Arrays of primitive types should be passed to the generic overload.
     * @throws MissingProtocolException the type of any member of [array]
     * is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(array: Array<out T>) {  // code size (code member)*
        OBJECT_ARRAY.mark(stream)
        writeIntNoMark(array.size)
        array.forEach { writeNonNull(it) }
    }

    /**
     * Writes all members in the list according the protocol of each.
     * Avoids null check for members, unlike generic `write`.
     * @throws MissingProtocolException any member of [list] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(list: List<T>) { // code size (code member)*
        LIST.mark(stream)
        writeIntNoMark(list.size)
        list.forEach { writeNonNull(it) }
    }

    /**
     * Writes all members in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of members.
     * Avoids null check for members, unlike generic `write`.
     * @throws MissingProtocolException any member of [iter] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(iter: Iterable<T>) { // code (code member)* sentinel
        ITERABLE.mark(stream)
        iter.forEach { writeNonNull(it) }
        SENTINEL.mark(stream)
    }

    /**
     * Writes the given pair according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     * @throws MissingProtocolException any member of [pair] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(pair: Pair<T,T>) {
        PAIR.mark(stream)
        writeNonNull(pair.first)
        writeNonNull(pair.second)
    }

    /**
     * Writes the given triple according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     * @throws MissingProtocolException any member of [triple] is not a top-level class or does not have a defined protocol
     */
    fun <T : Any> write(triple: Triple<T,T,T>) {
        TRIPLE.mark(stream)
        writeNonNull(triple.first)
        writeNonNull(triple.second)
        writeNonNull(triple.third)
    }

    /**
     * Writes the given map entry according to the protocols of its key and value.
     * Avoids null check for members, unlike generic `write`.
     * @throws MissingProtocolException any member of [entry] is not a top-level class or does not have a defined protocol
     */
    fun <K : Any, V : Any> write(entry: Map.Entry<K,V>) {
        MAP_ENTRY.mark(stream)
        writeNonNull(entry.key)
        writeNonNull(entry.value)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     * @throws MissingProtocolException if [obj] is not null, and
     * its type is not a top-level class or does not have a defined protocol
     */
    fun write(obj: Any?) {
        if (obj == null) {
            NULL.mark(stream)
            return
        }
        writeNonNull(obj)
    }

    private inline fun writeBytesNoMark(count: Int, write: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(count)
        write(buffer)
        stream.write(buffer.array())
    }

    private fun writeIntNoMark(n: Int) = writeBytesNoMark(Int.SIZE_BYTES) { putInt(n) }

    // code size member*
    private inline fun writeArrayNoMark(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1 * Int.SIZE_BYTES + size * memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }

    private fun writeStringNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeIntNoMark(bytes.size)
        stream.write(bytes)
    }

    private fun writeIterableNoMark(iter: Iterable<*>) {
        iter.forEach { it?.let { write(it) } ?: NULL.mark(stream) }
    }

    private fun writeNonNull(obj: Any) {
        val classRef = obj::class
        val className = classRef.nameIfExists() // Ensures eligible protocol
        defaultWriteOperations[className]?.let {
            this.it(obj)    // Marks stream
            return
        }
        OBJECT.mark(stream)
        val supertypes = protocols.supertypes.getOrDefault(classRef, listOf())
        stream.write(supertypes.size)   // There is no way someone's going to inherit more than 255 classes...
        supertypes.forEach {    // packets := (type object)*
            writeStringNoMark(it.qualifiedName!!)
            protocols.definedProtocols[it]?.write?.generic(this, obj)
                ?: throw MissingProtocolException("No write operation found for supertype '$it' of type '$className'")
        }
        val protocol = protocols.definedProtocols[classRef]
            ?: throw MissingProtocolException("No write operation found for protocol of type '$className'")
        writeStringNoMark(className)
        protocol.write.generic(this, obj)
    }

    private companion object {
        val defaultWriteOperations = mapOf(
            write<Boolean> {
                writeBoolean(it)
            },
            write<Byte> {
                writeByte(it)
            },
            write<Char> {
                writeChar(it)
            },
            write<Short> {
                writeShort(it)
            },
            write<Int> {
                writeInt(it)
            },
            write<Long> {
                writeLong(it)
            },
            write<Float> {
                writeFloat(it)
            },
            write<Double> {
                writeDouble(it)
            },
            write<BooleanArray> { obj ->
                BOOLEAN_ARRAY.mark(stream)
                obj.forEach { stream.write(if (it) 1 else 0) }
            },
            write<ByteArray> { obj ->
                BYTE_ARRAY.mark(stream)
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(obj.size) }
                obj.forEach { stream.write(it.toInt()) }
            },
            write<CharArray> { obj ->
                CHAR_ARRAY.mark(stream)
                writeArrayNoMark(Char.SIZE_BYTES, obj.size) { obj.forEach { putChar(it) } }
            },
            write<ShortArray> { obj ->
                SHORT_ARRAY.mark(stream)
                writeArrayNoMark(Short.SIZE_BYTES, obj.size) { obj.forEach { putShort(it) } }
            },
            write<IntArray> { obj ->
                INT_ARRAY.mark(stream)
                writeArrayNoMark(Int.SIZE_BYTES, obj.size) { obj.forEach { putInt(it) } }
            },
            write<LongArray> { obj ->
                LONG_ARRAY.mark(stream)
                writeArrayNoMark(Long.SIZE_BYTES, obj.size) { obj.forEach { putLong(it) } }
            },
            write<FloatArray> { obj ->
                FLOAT_ARRAY.mark(stream)
                writeArrayNoMark(Float.SIZE_BYTES, obj.size) { obj.forEach { putFloat(it) } }
            },
            write<DoubleArray> { obj ->
                DOUBLE_ARRAY.mark(stream)
                writeArrayNoMark(Double.SIZE_BYTES, obj.size) { obj.forEach { putDouble(it) } }
            },
            write<String> {
                STRING.mark(stream)
                writeStringNoMark(it)
            },
            write<Array<*>> { instance ->
                NULLABLES_ARRAY.mark(stream)
                writeIntNoMark(instance.size)
                instance.forEach { it?.let { write(it) } ?: NULL.mark(stream) }
            },
            write<List<*>> { instance ->
                NULLABLES_LIST.mark(stream)
                writeIntNoMark(instance.size)
                writeIterableNoMark(instance)
            },
            write<Iterable<*>> { instance ->
                NULLABLES_ITERABLE.mark(stream)
                writeIterableNoMark(instance)
                SENTINEL.mark(stream)
            },
            write<Pair<*, *>> {
                NULLABLES_PAIR.mark(stream)
                write(it.first)
                write(it.second)
            },
            write<Triple<*, *, *>> {
                NULLABLES_TRIPLE.mark(stream)
                write(it.first)
                write(it.second)
                write(it.third)
            },
            write<Map.Entry<*, *>> {
                NULLABLES_MAP_ENTRY.mark(stream)
                write(it.key)
                write(it.value)
            }
        )

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> write(noinline write: WriteOperation<T>) =
            T::class.qualifiedName!! to write as WriteOperation<Any>
    }
}