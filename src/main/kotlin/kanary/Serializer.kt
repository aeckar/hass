@file:Suppress("UNUSED")
package kanary

import kanary.TypeFlag.*
import java.io.Closeable
import java.io.Flushable
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

/**
 * See [Schema] for the types with pre-defined binary I/O protocols.
 * @return a new serializer capable of writing primitives, primitive arrays,
 * and instances of any type with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: Schema = Schema.EMPTY) = OutputSerializer(this, protocols)

fun Serializer.write(vararg objs: Any?) {
    objs.forEach { write(it) }
}

private inline fun <T> T.isNotNullAnd(predicate: (T & Any).() -> Boolean) = if (this == null) false else predicate(this)

@Suppress("UNCHECKED_CAST")
private fun WriteOperation<*>.accept(stream: OutputSerializer, obj: Any) = (this as WriteOperation<Any>)(stream, obj)

sealed interface Serializer {
    fun writeBoolean(cond: Boolean)
    fun writeByte(b: Byte)
    fun writeChar(c: Char)
    fun writeShort(n: Short)
    fun writeInt(n: Int)
    fun writeLong(n: Long)
    fun writeFloat(fp: Float)
    fun writeDouble(fp: Double)
    fun write(obj: Any?)

    // Optimizations for built-in composite types with non-nullable members
    fun <T : Any> write(array: Array<out T>)
    fun <T : Any> write(list: List<T>)
    fun <T : Any> write(iter: Iterable<T>)
    fun <T : Any> write(pair: Pair<T, T>)
    fun <T : Any> write(triple: Triple<T, T, T>)
    fun <K : Any, V : Any> write(entry: Map.Entry<K, V>)
    fun <K : Any, V : Any> write(map: Map<K, V>)
}

/**
 * Writes serialized data to a stream in Kanary format.
 * Does not need to be closed so long as the underlying stream is closed.
 * Because no protocols are defined, no instances of any reference types may be written.
 * Calling [close] also closes the underlying stream.
 * Until closed, instances are blocking.
 */
class OutputSerializer internal constructor(
    private var stream: OutputStream,
    private val schema: Schema
) : Closeable, Flushable, Serializer {
    override fun writeBoolean(cond: Boolean) {
        writeFlag(BOOLEAN)
        writeBooleanNoMark(cond)
    }

    override fun writeByte(b: Byte) {
        writeFlag(BYTE)
        writeByteNoMark(b)
    }

    override fun writeChar(c: Char) {
        writeFlag(CHAR)
        writeCharNoMark(c)
    }

    override fun writeShort(n: Short) {
        writeFlag(SHORT)
        writeShortNoMark(n)
    }

    override fun writeInt(n: Int) {
        writeFlag(INT)
        writeIntNoMark(n)
    }

    override fun writeLong(n: Long) {
        writeFlag(LONG)
        writeLongNoMark(n)
    }

    override fun writeFloat(fp: Float) {
        writeFlag(FLOAT)
        writeFloatNoMark(fp)
    }

    override fun writeDouble(fp: Double) {
        writeFlag(DOUBLE)
        writeDoubleNoMark(fp)
    }

    /**
     * Writes the object in binary format according to the protocol of its type, or null.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     */
    override fun write(obj: Any?) {
        if (obj == null) {
            writeFlag(NULL)
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
    override fun <T : Any> write(array: Array<out T>) = writeAny(array, nonNullMembers = true)

    /**
     * Writes all members in the list according the protocol of each.
     * Avoids null check for members, unlike generic `write`.
     */
    override fun <T : Any> write(list: List<T>) = writeAny(list, nonNullMembers = true)

    /**
     * Writes all members in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of members.
     * Avoids null check for members, unlike generic `write`.
     */
    override fun <T : Any> write(iter: Iterable<T>) = writeAny(iter, nonNullMembers = true)

    /**
     * Writes the given pair according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     */
    override fun <T : Any> write(pair: Pair<T, T>) = writeAny(pair, nonNullMembers = true)

    /**
     * Writes the given triple according to the protocols of its members.
     * Avoids null check for members, unlike generic `write`.
     */
    override fun <T : Any> write(triple: Triple<T, T, T>) = writeAny(triple, nonNullMembers = true)

    /**
     * Writes the given map entry according to the protocols of its key and value.
     * Avoids null check for members, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(entry: Map.Entry<K, V>) = writeAny(entry, nonNullMembers = true)

    /**
     * Writes the given map according to the protocols of its keys and values.
     * Avoids null check for entries, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(map: Map<K, V>) = writeAny(map, nonNullMembers = true)

    internal fun wrap(stream: OutputStream) = this.also { this.stream = stream }

    override fun close() = stream.close()
    override fun flush() = stream.flush()

    internal fun writeStringNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeIntNoMark(bytes.size)
        stream.write(bytes)
    }

    private fun writeFlag(flag: TypeFlag) {
        stream.write(flag.ordinal)
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

    // flag size member*
    private inline fun writeArrayNoMark(memberBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1 * Int.SIZE_BYTES + size * memberBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }

    private fun writeAny(obj: Any, nonNullMembers: Boolean = false) {
        fun writeBuiltIn(obj: Any, builtInKClass: KClass<*>, builtIns: Map<KClass<*>, BuiltInWriteSpecifier>) {
            builtIns.getValue(builtInKClass).let { (flag, write) ->
                writeFlag(flag)
                write.accept(this, obj)
            }
        }

        if (obj === Unit) {
            writeFlag(UNIT)
            return
        }
        if (obj is Function<*>) {
            writeFlag(FUNCTION)
            ObjectOutputStream(stream).writeObject(obj)
            return
        }
        val classRef = obj::class
        val className = classRef.className
            ?: throw MissingOperationException("Serialization of local and anonymous classes not supported")
        var protocol = schema.definedProtocols[classRef]
        val builtIns = if (nonNullMembers) builtInNonNullWrites else builtInWrites
        val builtInKClass: KClass<*>?
        val writeSequence: List<WriteSpecifier>
        if (protocol.isNotNullAnd { write != null && (hasNoinherit || hasStatic) }) {
            builtInKClass = null
            writeSequence = schema.writeSequences.getValue(classRef)
        } else {
            builtInKClass = builtIns.keys.find { classRef.isSubclassOf(it) }
            builtInKClass?.writeWithLength {   // Serialize object as built-in type
                writeBuiltIn(obj, it, builtIns)
                return
            }
            writeSequence = if (protocol != null) {
                schema.writeSequences.getValue(classRef)
            } else {
                schema.resolveWriteSequence(classRef.superclasses)
                    ?: throw MissingOperationException("Write operation for '$className' expected, but not found")
            }
        }
        val (kClass, write) = writeSequence.first()
        protocol = schema.definedProtocols.getValue(kClass)
        if (protocol.hasNoinherit) {
            writeFlag(SIMPLE_OBJECT)
            writeStringNoMark(className)
            write.accept(this, obj)
            return
        }
        writeFlag(OBJECT)
        if (protocol.hasStatic) {    // Necessary because static write overrides regular write sequence
            stream.write(0) // packet count
            writeWithLength {
                writeStringNoMark(className)
                write.accept(this, obj)
            }
            return
        }
        val customPacketCount = writeSequence.size - 1
        val packetCount = if (builtInKClass == null) customPacketCount else (customPacketCount + 1)
        stream.write(packetCount)
        repeat(customPacketCount) {
            val (packetKClass, packetWrite) = writeSequence[it + 1]
            writeWithLength {
                writeFlag(OBJECT)
                // Deserializer use only
                writeStringNoMark(packetKClass.className!!) // Deserializer use only
                packetWrite.accept(this, obj)
            }
        }
        builtInKClass?.writeWithLength {    // Built-in packet
            writeBuiltIn(obj, it, builtIns) // Marks stream with appropriate built-in flag
        }
        writeWithLength {
            writeStringNoMark(className)
            write.accept(this, obj)
        }
    }

    // Helps to reduce indentation
    private inline fun <T> T.writeWithLength(writeWithValue: OutputSerializer.(T) -> Unit) {
        val intermediate = ArrayOutputStream()
        writeWithValue(OutputSerializer(intermediate), this)
        writeIntNoMark(intermediate.size)
        intermediate.writeTo(stream)
    }

    private fun OutputSerializer(stream: ArrayOutputStream) = OutputSerializer(stream, schema)

    private companion object {
        // Optimizations for built-in composite types avoiding null-checks for members
        val builtInNonNullWrites = linkedMapOf( // Preserve iteration order
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
            write(PAIR) { pair: Pair<Any, Any> ->
                writeAny(pair.first)
                writeAny(pair.second)
            },
            write(TRIPLE) { triple: Triple<Any, Any, Any> ->
                writeAny(triple.first)
                writeAny(triple.second)
                writeAny(triple.third)
            },
            write(MAP_ENTRY) { entry: Map.Entry<Any, Any> ->
                writeAny(entry.key)
                writeAny(entry.value)
            },
            write(MAP) { map: Map<Any, Any> ->   // Multi-maps not supported by default
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
        val builtInWrites = linkedMapOf(
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
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
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
                list.forEach { it?.let { write(it) } ?: run {
                    writeFlag(NULL)
                }
                }
            },
            write(ITERABLE) { iter: Iterable<*> ->
                var size = 0
                val intermediate = ArrayOutputStream()
                val serializer = OutputSerializer(intermediate)
                iter.forEach {
                    it?.let { serializer.write(it) } ?: run {
                        serializer.writeFlag(NULL)
                    }
                    ++size
                }
                writeIntNoMark(size)
                intermediate.writeTo(stream)
            },
            write(PAIR) { pair: Pair<*, *> ->
                write(pair.first)
                write(pair.second)
            },
            write(TRIPLE) { triple: Triple<*, *, *> ->
                write(triple.first)
                write(triple.second)
                write(triple.third)
            },
            write(MAP_ENTRY) { entry: Map.Entry<*, *> ->
                write(entry.key)
                write(entry.value)
            },
            write(MAP) { map: Map<*, *> ->   // Multi-maps not supported
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            }
        )

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> write(
            flag: TypeFlag,
            noinline write: OutputSerializer.(T) -> Unit
        ): Pair<KClass<*>, BuiltInWriteSpecifier> =
            T::class to BuiltInWriteSpecifier(flag, write as WriteOperation<Any>)
    }
}

private data class BuiltInWriteSpecifier(val flag: TypeFlag, val write: WriteOperation<*>)