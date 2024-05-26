package io.github.aeckar.kanary

import io.github.aeckar.kanary.TypeFlag.*
import io.github.aeckar.kanary.utils.decIf
import io.github.aeckar.kanary.utils.incIf
import io.github.aeckar.kanary.utils.jvmName
import java.io.*
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Writes serialized data to a stream in Kanary format.
 *
 * Does not need to be closed so long as the underlying stream is closed.
 * Calling [close] also closes the underlying stream; [flush] works similarly.
 */
class OutputSerializer(
    private val stream: OutputStream,
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
     * Writes the object in binary format according to the protocol of its type.
     *
     * Null objects are accepted, however their non-nullable type information is erased.
     * If the object is not null and its type does not have a defined protocol, the protocol of its superclass or
     * the first interface declared in source code with a protocol is chosen.
     * @throws MissingOperationException any object of an anonymous or local class,
     * or an appropriate write operation cannot be found
     */
    override fun write(obj: Any?) {
        if (obj == null) {
            writeFlag(NULL)
            return
        }
        writeObject(obj)
    }

    /**
     * Writes all elements in array according to the protocol of each instance.
     *
     * Avoids null check for elements, unlike generic `write`.
     * Arrays of primitive types should be passed to the generic overload.
     */
    override fun <T : Any> write(array: Array<out T>) = writeObject(array, nonNullElements = true)

    /**
     * Writes all elements in the list according the protocol of each.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(list: List<T>) = writeObject(list, nonNullElements = true)

    /**
     * Writes all elements in the iterable object according the protocol of each as a list.
     *
     * The caller must ensure that the object has a finite number of elements.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(iter: Iterable<T>) = writeObject(iter, nonNullElements = true)

    /**
     * Writes the given pair according to the protocols of its elements.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(pair: Pair<T, T>) = writeObject(pair, nonNullElements = true)

    /**
     * Writes the given triple according to the protocols of its elements.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(triple: Triple<T, T, T>) = writeObject(triple, nonNullElements = true)

    /**
     * Writes the given map entry according to the protocols of its key and value.
     *
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(entry: Map.Entry<K, V>) = writeObject(entry, nonNullElements = true)

    /**
     * Writes the given map according to the protocols of its keys and values.
     *
     * Avoids null check for entries, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(map: Map<K, V>) = writeObject(map, nonNullElements = true)

    /**
     * Closes the underlying stream and releases any system resources associated with it.
     *
     * If the stream is already closed then invoking this method has no effect.
     */
    override fun close() = stream.close()

    /**
     * Flushes the underlying stream and forces any buffered output bytes to be written out.
     */
    override fun flush() = stream.flush()

    /**
     * ```
     * noMarkString := (size: i32)(bytes: byte[]?)
     * ```
     */
    private fun writeStringNoMark(s: String) {
        val bytes = s.toByteArray(Charsets.UTF_8)
        writeIntNoMark(bytes.size)
        stream.write(bytes)
    }

    /**
     * ```
     * flag := (ordinal: i8)
     * ```
     */
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

    /**
     * ```
     * noMarkArray := (size: i32)(element: object*)
     * ```
     */
    private inline fun writeArrayNoMark(elementBytes: Int, size: Int, bulkWrite: ByteBuffer.() -> Unit) {
        val buffer = ByteBuffer.allocate(1 * Int.SIZE_BYTES + size * elementBytes)
        buffer.putInt(size)
        bulkWrite(buffer)
        stream.write(buffer.array())
    }

    /**
     * ```
     * builtIn := (marker: flag)(builtIn: ...?)
     *
     * superData := (begin: flag = OBJECT)(className: noMarkString)(userData: ...)
     * (end: flag = END_OBJECT)
     *
     * object := (begin: flag = OBJECT)(className: noMarkString)(superCount: i8)
     * (supers: superData*)(builtInSuper: builtIn?)(userData: ...)(end: flag = END_OBJECT)
     * ```
     */
    private fun writeObject(obj: Any, nonNullElements: Boolean = false) {
        fun KClass<*>.writeBuiltIn(obj: Any, builtIns: Map<KClass<*>, WriteMapping>) {
            builtIns.getValue(this).let { (flag, lambda) ->
                writeFlag(flag)
                this@OutputSerializer.lambda(obj)
            }
        }

        if (obj is Function<*>) {   // Necessary because lambdas lack qualified names
            writeFlag(FUNCTION)
            if (KotlinVersion.CURRENT.major >= 2 && obj !is Serializable) {
                throw NotSerializableException("Lambdas must be annotated with @JvmSerializableLambda to be serialized")
            }
            ObjectOutputStream(stream).writeObject(obj)
            return
        }
        val classRef = obj::class
        val className = classRef.jvmName
            ?: throw NotSerializableException("Serialization of local and anonymous class instances not supported")
        val protocol = schema.protocols[classRef]
        val builtIns = BuiltInWriteOperations given nonNullElements
        val builtInKClass: KClass<*>?
        val writeMap: WriteMap
        if (protocol != null && protocol.hasStatic) {
            builtInKClass = null
            writeMap = schema.writeMapOf(classRef)
        } else {
            builtInKClass = builtIns.keys.find { classRef.isSubclassOf(it) }
            writeMap = if (protocol != null) {
                schema.writeMapOf(classRef)
            } else {
                builtInKClass?.writeBuiltIn(obj, builtIns)?.let { return }  // Serialize as built-in type
                schema.writeMapOf(classRef)
            }
        }
        val (kClass, lambda) = writeMap.entries.first()
        val hasWrite = kClass == classRef   // If true, first entry in map is class, write operation of 'obj'
        val nonBuiltInSupers = writeMap.size decIf hasWrite
        val totalSupers = nonBuiltInSupers incIf (builtInKClass != null)
        writeFlag(OBJECT)
        writeStringNoMark(className)
        stream.write(totalSupers)
        if (nonBuiltInSupers != 0) {
            val entries = writeMap.iterator().also { if (hasWrite) it.next() }
            repeat(nonBuiltInSupers) {
                val (superKClass, superLambda) = entries.next()
                writeFlag(OBJECT)
                writeStringNoMark(superKClass.jvmName!!)
                this.superLambda(obj)
                writeFlag(END_OBJECT)
            }
            builtInKClass?.writeBuiltIn(obj, builtIns) // Marks stream with appropriate built-in flag
        }
        this.lambda(obj)
        writeFlag(END_OBJECT)
    }

    private object BuiltInWriteOperations {
        private val nonNullBuiltInWrites = mapOf(
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<Any> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { writeObject(it) }
            },
            builtInWriteOf(LIST) { list: List<Any> ->
                writeIntNoMark(list.size)
                list.forEach { writeObject(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<Any> ->
                iter.forEach { writeObject(it) }
                writeFlag(END_OBJECT)
            },
            builtInWriteOf(PAIR) { pair: Pair<Any, Any> ->
                writeObject(pair.first)
                writeObject(pair.second)
            },
            builtInWriteOf(TRIPLE) { triple: Triple<Any, Any, Any> ->
                writeObject(triple.first)
                writeObject(triple.second)
                writeObject(triple.third)
            },
            builtInWriteOf(MAP_ENTRY) { entry: Map.Entry<Any, Any> ->
                writeObject(entry.key)
                writeObject(entry.value)
            },
            builtInWriteOf(MAP) { map: Map<Any, Any> ->   // Multi-maps not supported by default
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    writeObject(key)
                    writeObject(value)
                }
            }
        )

        private val nullableBuiltInWrites = mapOf(
            builtInWriteOf<Unit>(UNIT) {},
            builtInWriteOf(BOOLEAN) { value: Boolean ->
                writeBooleanNoMark(value)
            },
            builtInWriteOf(BYTE) { value: Byte ->
                writeByteNoMark(value)
            },
            builtInWriteOf(CHAR) { value: Char ->
                writeCharNoMark(value)
            },
            builtInWriteOf(SHORT) { value: Short ->
                writeShortNoMark(value)
            },
            builtInWriteOf(INT) { value: Int ->
                writeIntNoMark(value)
            },
            builtInWriteOf(LONG) { value: Long ->
                writeLong(value)
            },
            builtInWriteOf(FLOAT) { value: Float ->
                writeFloat(value)
            },
            builtInWriteOf(DOUBLE) { value: Double ->
                writeDouble(value)
            },
            builtInWriteOf(BOOLEAN_ARRAY) { array: BooleanArray ->
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
                array.forEach { stream.write(if (it) 1 else 0) }
            },
            builtInWriteOf(BYTE_ARRAY) { array: ByteArray ->
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
                array.forEach { stream.write(it.toInt()) }
            },
            builtInWriteOf(CHAR_ARRAY) { array: CharArray ->
                writeArrayNoMark(Char.SIZE_BYTES, array.size) { array.forEach { putChar(it) } }
            },
            builtInWriteOf(SHORT_ARRAY) { array: ShortArray ->
                writeArrayNoMark(Short.SIZE_BYTES, array.size) { array.forEach { putShort(it) } }
            },
            builtInWriteOf(INT_ARRAY) { array: IntArray ->
                writeArrayNoMark(Int.SIZE_BYTES, array.size) { array.forEach { putInt(it) } }
            },
            builtInWriteOf(LONG_ARRAY) { array: LongArray ->
                writeArrayNoMark(Long.SIZE_BYTES, array.size) { array.forEach { putLong(it) } }
            },
            builtInWriteOf(FLOAT_ARRAY) { array: FloatArray ->
                writeArrayNoMark(Float.SIZE_BYTES, array.size) { array.forEach { putFloat(it) } }
            },
            builtInWriteOf(DOUBLE_ARRAY) { array: DoubleArray ->
                writeArrayNoMark(Double.SIZE_BYTES, array.size) { array.forEach { putDouble(it) } }
            },
            builtInWriteOf(STRING) { s: String ->
                writeStringNoMark(s)
            },
            builtInWriteOf(OBJECT_ARRAY) { objArray: Array<*> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { write(it) }
            },
            builtInWriteOf(LIST) { list: List<*> ->
                writeIntNoMark(list.size)
                list.forEach { write(it) }
            },
            builtInWriteOf(ITERABLE) { iter: Iterable<*> ->
                iter.forEach { write(it) }
                writeFlag(END_OBJECT)
            },
            builtInWriteOf(PAIR) { pair: Pair<*, *> ->
                write(pair.first)
                write(pair.second)
            },
            builtInWriteOf(TRIPLE) { triple: Triple<*, *, *> ->
                write(triple.first)
                write(triple.second)
                write(triple.third)
            },
            builtInWriteOf(MAP_ENTRY) { entry: Map.Entry<*, *> ->
                write(entry.key)
                write(entry.value)
            },
            builtInWriteOf(MAP) { map: Map<*, *> ->
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            }
        )

        /**
         * If [nonNullElements] is true, returns the optimized [built-in write handles][WriteMapping]
         * for composite types with non-null members. Otherwise, returns the built-in write handles
         * for all types with pre-defined protocols agreeing with the list in [Schema].
         * The returned map iterates in order of insertion,
         * with the write handle for [List] coming before that of [Iterable].
         * @return map of built-in write handles according to their [flag][TypeFlag] and element nullability
         */
        infix fun given(nonNullElements: Boolean) = if (nonNullElements) nonNullBuiltInWrites else nullableBuiltInWrites

        @Suppress("UNCHECKED_CAST")
        private inline fun <reified T : Any> builtInWriteOf(
            flag: TypeFlag,
            noinline write: OutputSerializer.(T) -> Unit
        ): Pair<KClass<*>, WriteMapping> =
            T::class to WriteMapping(flag, write as WriteOperation)
    }
}

/**
 * Specifies the [flag] from where the given [write operation][lambda] originates from.
 */
private data class WriteMapping(val flag: TypeFlag, val lambda: WriteOperation)