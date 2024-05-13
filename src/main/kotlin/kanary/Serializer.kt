@file:Suppress("UNUSED")
package kanary

import kanary.TypeCode.*
import java.io.Closeable
import java.io.Flushable
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * See [Schema] for the types with pre-defined binary I/O protocols.
 * @return a new serializer capable of writing primitives, primitive arrays,
 * and instances of any type with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: Schema = Schema.EMPTY) = Serializer(this, protocols)

@Suppress("UNCHECKED_CAST")
private fun WriteOperation<*>.accept(stream: Serializer, obj: Any) = (this as WriteOperation<Any>)(stream, obj)

/**
 * Writes serialized data to a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Because no protocols are defined, no instances of any reference types may be written.
 * Calling [close] also closes the underlying stream.
 */
class Serializer internal constructor(
    private var stream: OutputStream,
    private val protocols: Schema
) : Closeable, Flushable {
    fun writeBoolean(cond: Boolean) {
        BOOLEAN.mark(stream)
        writeBooleanNoMark(cond)
    }

    fun writeByte(b: Byte) {
        BYTE.mark(stream)
        writeByteNoMark(b)
    }

    fun writeChar(c: Char) {
        CHAR.mark(stream)
        writeCharNoMark(c)
    }

    fun writeShort(n: Short) {
        SHORT.mark(stream)
        writeShortNoMark(n)
    }

    fun writeInt(n: Int) {
        INT.mark(stream)
        writeIntNoMark(n)
    }

    fun writeLong(n: Long) {
        LONG.mark(stream)
        writeLongNoMark(n)
    }

    fun writeFloat(fp: Float) {
        FLOAT.mark(stream)
        writeFloatNoMark(fp)
    }

    fun writeDouble(fp: Double) {
        DOUBLE.mark(stream)
        writeDoubleNoMark(fp)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     */
    fun write(obj: Any?) {
        if (obj == null) {
            NULL.mark(stream)
            return
        }
        writeAny(obj)
    }

    // Optimizations for built-in composite types with non-nullable members...

    /**
     * Writes all members in array according to the protocol of each instance.
     * Avoids null check for members, unlike generic `write`.
     * Arrays of primitive types should be passed to the generic overload.
     */
    fun <T : Any> write(array: Array<out T>) = writeAny(array, nonNullMembers = true)

    /**
     * Writes all members in the list according the protocol of each.
     * Avoids null check for members, unlike generic `write`.
     */
    fun <T : Any> write(list: List<T>) = writeAny(list, nonNullMembers = true)

    /**
     * Writes all members in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of members.
     * Avoids null check for members, unlike generic `write`.
     */
    fun <T : Any> write(iter: Iterable<T>) = writeAny(iter, nonNullMembers = true)

    /**
     * Writes the given pair according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     */
    fun <T : Any> write(pair: Pair<T,T>) = writeAny(pair, nonNullMembers = true)

    /**
     * Writes the given triple according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     */
    fun <T : Any> write(triple: Triple<T,T,T>) = writeAny(triple, nonNullMembers = true)

    /**
     * Writes the given map entry according to the protocols of its key and value.
     * Avoids null check for members, unlike generic `write`.
     */
    fun <K : Any, V : Any> write(entry: Map.Entry<K,V>) = writeAny(entry, nonNullMembers = true)

    /**
     * Writes the given map according to the protocols of its keys and values.
     * Avoids null check for entries, unlike generic `write`.
     */
    fun <K : Any, V : Any> write(map: Map<K,V>) = writeAny(map, nonNullMembers = true)

    internal fun wrap(stream: OutputStream) = this.also { this.stream = stream }

    override fun close() = stream.close()
    override fun flush() = stream.flush()

    // Non-marking functions...

    internal fun writeStringNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeIntNoMark(bytes.size)
        stream.write(bytes)
    }

    private inline fun writeBytesNoMark(count: Int, write: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(count)
        write(buffer)
        stream.write(buffer.array())
    }

    private fun writeBooleanNoMark(cond: Boolean) = stream.write(if (cond) 1 else 0)
    private fun writeByteNoMark(b: Byte) = stream.write(b.toInt())
    private fun writeCharNoMark(c: Char) = writeBytesNoMark(Char.SIZE_BYTES) { putChar(c) }
    private fun writeShortNoMark(n: Short) = writeBytesNoMark(Short.SIZE_BYTES) { putShort(n) }
    private fun writeIntNoMark(n: Int) = writeBytesNoMark(Int.SIZE_BYTES) { putInt(n) }
    private fun writeLongNoMark(n: Long) = writeBytesNoMark(Long.SIZE_BYTES) { putLong(n) }
    private fun writeFloatNoMark(fp: Float) = writeBytesNoMark(Float.SIZE_BYTES) { putFloat(fp) }
    private fun writeDoubleNoMark(fp: Double) = writeBytesNoMark(Double.SIZE_BYTES) { putDouble(fp) }

    // code size member*
    private inline fun writeArrayNoMark(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1 * Int.SIZE_BYTES + size * memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }

    /* Separate function since 'null' does not have a class, and therefore cannot be searched in the map.
     * Comes with the added benefit of being able to be used whenever
     * a member of a composite type is guaranteed to not be null.
     */

    /* information := typeName object
     * customPacket := code typeName object sentinel
     * builtInPacket := builtInCode builtInInformation
     * object :=
     *  builtInCode object
     *  | code packetCount customPacket* builtInPacket? information sentinel
     */
    private fun writeAny(obj: Any, nonNullMembers: Boolean = false) {
        fun List<JvmType>.writeSequence(allSequences: Map<JvmClass, ProtocolSequence>): ProtocolSequence {
            asSequence()
                .map { it.jvmErasure }
                .forEach { jvmClass ->
                    allSequences[jvmClass]?.let { return it }
                }
            for (jvmType in this) {
                val result = jvmType.jvmErasure.supertypes.writeSequence(allSequences)
                if (result.isNotEmpty()) {
                    return result
                }
            }
            return listOf()
        }

        fun writeBuiltIn(obj: Any, builtInType: JvmClass,
            builtIns: Map<JvmClass, Pair<TypeCode, WriteOperation<Any>>>) {
            builtIns.getValue(builtInType).let { (code, write) ->
                code.mark(stream)
                write.accept(this, obj)
            }
        }

        if (obj === Unit) {
            UNIT.mark(stream)
            return
        }
        val classRef = obj::class
        val className = classRef.qualifiedName ?: throw MalformedProtocolException(classRef,
            "local or anonymous") // Ensures eligible protocol
        var writeSequence = protocols.writeSequences.getOrDefault(classRef, emptyList())
        val builtIns = if (nonNullMembers) builtInNonNullWrites else builtInWrites
        val builtInSupertype = builtIns.keys.find { classRef.isSubclassOf(it) }
        if (writeSequence.isEmpty()) {
            builtInSupertype?.let { jvmType ->  // Serialize object as built-in type
                writeWithLength {
                    writeBuiltIn(obj, jvmType, builtIns)
                }
                return
            }
            writeSequence = classRef.supertypes.writeSequence(protocols.writeSequences) // Can be empty
        }
        OBJECT.mark(stream)
        val objProtocol = writeSequence.lastOrNull()?.second
        if (objProtocol?.isWriteStatic != false) {  // Necessary because static write overrides regular write sequence
            stream.write(0) // packet count
            writeWithLength {
                writeStringNoMark(className)
                objProtocol?.write?.accept(this, obj)
            }
            return  // No information serialized if writeSequence.isEmpty()
        }
        val customPacketCount = writeSequence.size - 1
        val packetCount = if (builtInSupertype == null) customPacketCount else (customPacketCount + 1)
        stream.write(packetCount)
        repeat(customPacketCount) {
            val (typeName, protocol) = writeSequence[it]
            protocol.write?.let { write ->  // packet := typeName object
                writeWithLength {
                    OBJECT.mark(stream)
                    writeStringNoMark(typeName)
                    write.accept(this, obj)
                }
            }
        }
        builtInSupertype?.let { // Built-in packet
            writeWithLength {
                writeBuiltIn(obj, it, builtIns) // Marks stream with appropriate built-in code
            }
        }
        writeWithLength {
            writeStringNoMark(className)
            objProtocol.write?.accept(this, obj)
        }
    }

    private inline fun writeWithLength(write: Serializer.() -> Unit) {
        val intermediate = ArrayOutputStream()
        Serializer(intermediate, protocols).apply(write)
        writeIntNoMark(intermediate.size)
        stream.write(intermediate.bytes)
    }

    @PublishedApi
    internal companion object {
        // Optimizations for built-in composite types avoiding null-checks for members
        private val builtInNonNullWrites = mapOf(
            write(OBJECT_ARRAY) { objArray: Array<Any> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { writeAny(it) }
            },
            write(LIST) { list: List<Any> ->
                writeIntNoMark(list.size)
                list.forEach { writeAny(it) }
            },
            write(ITERABLE) { iter: Iterable<Any> ->
                writeWithLength {
                    iter.forEach { writeAny(it) }
                }
            },
            write(PAIR) { pair: Pair<Any,Any> ->
                writeAny(pair.first)
                writeAny(pair.second)
            },
            write(TRIPLE) { triple: Triple<Any,Any,Any> ->
                writeAny(triple.first)
                writeAny(triple.second)
                writeAny(triple.third)
            },
            write(MAP_ENTRY) { entry: Map.Entry<Any,Any> ->
                writeAny(entry.key)
                writeAny(entry.value)
            },
            write(MAP) { map: Map<Any,Any> ->   // Multi-maps not supported by default
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    writeAny(key)
                    writeAny(value)
                }
            }
        )

        /* Exclusive to a single object
         * if an iterable is a list, it is written using write<List> { ... }
         */
        val builtInWrites = mapOf(
            write(BOOLEAN) { value: Boolean ->
                writeBooleanNoMark(value)   // Separate function that prevents autoboxing
            },
            write(BYTE) { value: Byte ->
                writeByteNoMark(value)
            },
            write(CHAR) { value: Char ->
                writeCharNoMark(value)
            },
            write(SHORT) { value: Short ->
                writeShortNoMark(value)
            },
            write(INT) { value: Int ->
                writeIntNoMark(value)
            },
            write(LONG) { value: Long ->
                writeLong(value)
            },
            write(FLOAT) { value: Float ->
                writeFloat(value)
            },
            write(DOUBLE) { value: Double ->
                writeDouble(value)
            },
            write(BOOLEAN_ARRAY) { array: BooleanArray ->
                array.forEach { stream.write(if (it) 1 else 0) }
            },
            write(BYTE_ARRAY) { array: ByteArray ->
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
                array.forEach { stream.write(it.toInt()) }
            },
            write(CHAR_ARRAY) { array: CharArray ->
                writeArrayNoMark(Char.SIZE_BYTES, array.size) { array.forEach { putChar(it) } }
            },
            write(SHORT_ARRAY) { array: ShortArray ->
                writeArrayNoMark(Short.SIZE_BYTES, array.size) { array.forEach { putShort(it) } }
            },
            write(INT_ARRAY) { array: IntArray ->
                writeArrayNoMark(Int.SIZE_BYTES, array.size) { array.forEach { putInt(it) } }
            },
            write(LONG_ARRAY) { array: LongArray ->
                writeArrayNoMark(Long.SIZE_BYTES, array.size) { array.forEach { putLong(it) } }
            },
            write(FLOAT_ARRAY) { array: FloatArray ->
                writeArrayNoMark(Float.SIZE_BYTES, array.size) { array.forEach { putFloat(it) } }
            },
            write(DOUBLE_ARRAY) { array: DoubleArray ->
                writeArrayNoMark(Double.SIZE_BYTES, array.size) { array.forEach { putDouble(it) } }
            },
            write(STRING) { s: String ->
                writeStringNoMark(s)
            },
            write(OBJECT_ARRAY) { objArray: Array<*> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { write(it) }
            },
            write(LIST) { list: List<*> ->
                writeIntNoMark(list.size)
                list.forEach { it?.let { write(it) } ?: NULL.mark(stream) }
            },
            write(ITERABLE) { iter: Iterable<*> ->
                writeWithLength {
                    iter.forEach { it?.let { write(it) } ?: NULL.mark(stream) }
                }
            },
            write(PAIR) { pair: Pair<*,*> ->
                write(pair.first)
                write(pair.second)
            },
            write(TRIPLE) { triple: Triple<*,*,*> ->
                write(triple.first)
                write(triple.second)
                write(triple.third)
            },
            write(MAP_ENTRY) { entry: Map.Entry<*,*> ->
                write(entry.key)
                write(entry.value)
            },
            write(MAP) { map: Map<*,*> ->   // Multi-maps not supported
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            }
        )

        @Suppress("UNCHECKED_CAST")
        private inline fun <reified T : Any> write(code: TypeCode, noinline write: WriteOperation<T>) =
            T::class to (code to write as WriteOperation<Any>)
    }
}