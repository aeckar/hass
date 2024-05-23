package kanary

import kanary.TypeFlag.*
import kanary.utils.jvmName
import java.io.*
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * See [Schema] for a list of types that can be serialized by default.
 * @return a new serializer capable of writing primitives, primitive arrays,
 * and instances of any type with a defined protocol to Kanary format
 */
fun OutputStream.serializer(protocols: Schema) = OutputSerializer(this, protocols)

/**
 * Writes the objects in binary format according to the protocol of each type.
 *
 * Null objects are accepted, however their non-nullable type information is erased.
 * If an object is not null and its type does not have a defined protocol, the protocol of its superclass or
 * the first interface declared in source code with a protocol is chosen.
 * If no objects are supplied, nothing is serialized.
 * @throws MissingOperationException any object of an anonymous or local class,
 * or an appropriate write operation cannot be found
 */
fun Serializer.write(vararg objs: Any?) {
    objs.forEach { write(it) }
}

/**
 * Thrown when an attempt is made to serialize an object that cannot be serialized due to the nature of its type.
 * The type may be local, anonymous, or a lambda not annotated with [JvmSerializableLambda].
 */
class NotSerializableException(message: String) : IOException(message)

/**
 * Serializes data to a stream in Kanary format.
 */
sealed interface Serializer {
    /**
     * Serializes the value without autoboxing.
     */
    fun writeBoolean(cond: Boolean)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeByte(b: Byte)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeChar(c: Char)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeShort(n: Short)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeInt(n: Int)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeLong(n: Long)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeFloat(fp: Float)

    /**
     * Serializes the value without autoboxing.
     */
    fun writeDouble(fp: Double)

    /**
     * Serializes the object or boxed primitive value.
     * @throws MissingOperationException obj is not an instance of a top-level or nested class,
     * or a suitable write operation cannot be determined
     */
    fun write(obj: Any?)

    /**
     * Serializes the array without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(array: Array<out T>)

    /**
     * Serializes the list without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(list: List<T>)

    /**
     * Serializes the iterable without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(iter: Iterable<T>)

    /**
     * Serializes the pair without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(pair: Pair<T, T>)

    /**
     * Serializes the triple without checking for null elements.
     * @throws MissingOperationException any element is not an instance of a top-level or nested class,
     * or a suitable write operation for it cannot be determined
     */
    fun <T : Any> write(triple: Triple<T, T, T>)

    /**
     * Serializes the map entry without checking for null elements.
     * @throws MissingOperationException the key or value is not an instance of a top-level or nested class,
     * or a suitable write operation for either cannot be determined
     */
    fun <K : Any, V : Any> write(entry: Map.Entry<K, V>)

    /**
     * Serializes the map without checking for null keys or values.
     * @throws MissingOperationException any key or value is not an instance of a top-level or nested class,
     * or a suitable write operation for any cannot be determined
     */
    fun <K : Any, V : Any> write(map: Map<K, V>)
}

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
     * Avoids null check for elements, unlike generic `write`.
     * Arrays of primitive types should be passed to the generic overload.
     */
    override fun <T : Any> write(array: Array<out T>) = writeObject(array, nonNullElements = true)

    /**
     * Writes all elements in the list according the protocol of each.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(list: List<T>) = writeObject(list, nonNullElements = true)

    /**
     * Writes all elements in the iterable object according the protocol of each as a list.
     * The caller must ensure that the object has a finite number of elements.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(iter: Iterable<T>) = writeObject(iter, nonNullElements = true)

    /**
     * Writes the given pair according to the protocols of its elements.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(pair: Pair<T, T>) = writeObject(pair, nonNullElements = true)

    /**
     * Writes the given triple according to the protocols of its elements.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <T : Any> write(triple: Triple<T, T, T>) = writeObject(triple, nonNullElements = true)

    /**
     * Writes the given map entry according to the protocols of its key and value.
     * Avoids null check for elements, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(entry: Map.Entry<K, V>) = writeObject(entry, nonNullElements = true)

    /**
     * Writes the given map according to the protocols of its keys and values.
     * Avoids null check for entries, unlike generic `write`.
     */
    override fun <K : Any, V : Any> write(map: Map<K, V>) = writeObject(map, nonNullElements = true)

    override fun close() = stream.close()
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
        fun KClass<*>.writeBuiltIn(obj: Any, builtIns: Map<KClass<*>, BuiltInWriteHandle>) {
            builtIns.getValue(this).let { (flag, lambda) ->
                writeFlag(flag)
                this@OutputSerializer.lambda(obj)
            }
        }

        val classRef = obj::class
        if (obj is Function<*>) {   // Necessary because lambdas lack qualified names
            writeFlag(FUNCTION)
            if (KotlinVersion.CURRENT.major >= 2 && obj !is Serializable) {
                throw NotSerializableException("Lambdas must be annotated with @JvmSerializableLambda to be serialized")
            }
            ObjectOutputStream(stream).writeObject(obj)
            return
        }
        val className = classRef.jvmName
            ?: throw NotSerializableException("Serialization of local and anonymous class instances not supported")
        var protocol = schema.protocolOrNull(classRef)
        val builtIns = if (nonNullElements) nonNullBuiltIns else nullableBuiltIns
        val builtInKClass: KClass<*>?
        val definedHandles: Set<WriteHandle>
        if (protocol != null && protocol.hasStatic) {
            builtInKClass = null
            definedHandles = schema.writeSequenceOf(classRef)
        } else {
            builtInKClass = builtIns.keys.find { classRef.isSubclassOf(it) }
            definedHandles = if (protocol != null) {
                schema.writeSequenceOf(classRef)
            } else {
                builtInKClass?.writeBuiltIn(obj, builtIns)?.let { return }  // Serialize as built-in type
                schema.writeSequenceOf(classRef)
            }
        }
        val (kClass, lambda) = definedHandles.first()
        protocol = schema.protocolOrNull(kClass)!!
        writeFlag(OBJECT)
        writeStringNoMark(className)
        val customSuperCount = definedHandles.size - 1
        assert(!protocol.hasStatic || customSuperCount == 0)
        val superCount = if (builtInKClass == null|| protocol.hasStatic) customSuperCount else (customSuperCount + 1)
        stream.write(superCount)
        if (customSuperCount != 0) {
            val handles = definedHandles.iterator().also { it.next() }
            repeat(customSuperCount) {
                val (superKClass, superLambda) = handles.next()
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

    private companion object {
        /**
         * Built-in write operations for objects with non-null elements.
         * Optimized to avoid unnecessary null checks.
         * @see nullableBuiltIns
         */
        val nonNullBuiltIns = linkedMapOf(
            builtInOf(OBJECT_ARRAY) { objArray: Array<Any> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { writeObject(it) }
            },
            builtInOf(LIST) { list: List<Any> ->
                writeIntNoMark(list.size)
                list.forEach { writeObject(it) }
            },
            builtInOf(ITERABLE) { iter: Iterable<Any> ->
                iter.forEach { writeObject(it) }
                writeFlag(END_OBJECT)
            },
            builtInOf(PAIR) { pair: Pair<Any, Any> ->
                writeObject(pair.first)
                writeObject(pair.second)
            },
            builtInOf(TRIPLE) { triple: Triple<Any, Any, Any> ->
                writeObject(triple.first)
                writeObject(triple.second)
                writeObject(triple.third)
            },
            builtInOf(MAP_ENTRY) { entry: Map.Entry<Any, Any> ->
                writeObject(entry.key)
                writeObject(entry.value)
            },
            builtInOf(MAP) { map: Map<Any, Any> ->   // Multi-maps not supported by default
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    writeObject(key)
                    writeObject(value)
                }
            }
        )

        /**
         * Built-in write operations.
         * @see nonNullBuiltIns
         */
        val nullableBuiltIns = linkedMapOf(
            builtInOf<Unit>(UNIT) {},
            builtInOf(BOOLEAN) { value: Boolean ->
                writeBooleanNoMark(value)
            },
            builtInOf(BYTE) { value: Byte ->
                writeByteNoMark(value)
            },
            builtInOf(CHAR) { value: Char ->
                writeCharNoMark(value)
            },
            builtInOf(SHORT) { value: Short ->
                writeShortNoMark(value)
            },
            builtInOf(INT) { value: Int ->
                writeIntNoMark(value)
            },
            builtInOf(LONG) { value: Long ->
                writeLong(value)
            },
            builtInOf(FLOAT) { value: Float ->
                writeFloat(value)
            },
            builtInOf(DOUBLE) { value: Double ->
                writeDouble(value)
            },
            builtInOf(BOOLEAN_ARRAY) { array: BooleanArray ->
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
                array.forEach { stream.write(if (it) 1 else 0) }
            },
            builtInOf(BYTE_ARRAY) { array: ByteArray ->
                writeBytesNoMark(Int.SIZE_BYTES) { putInt(array.size) }
                array.forEach { stream.write(it.toInt()) }
            },
            builtInOf(CHAR_ARRAY) { array: CharArray ->
                writeArrayNoMark(Char.SIZE_BYTES, array.size) { array.forEach { putChar(it) } }
            },
            builtInOf(SHORT_ARRAY) { array: ShortArray ->
                writeArrayNoMark(Short.SIZE_BYTES, array.size) { array.forEach { putShort(it) } }
            },
            builtInOf(INT_ARRAY) { array: IntArray ->
                writeArrayNoMark(Int.SIZE_BYTES, array.size) { array.forEach { putInt(it) } }
            },
            builtInOf(LONG_ARRAY) { array: LongArray ->
                writeArrayNoMark(Long.SIZE_BYTES, array.size) { array.forEach { putLong(it) } }
            },
            builtInOf(FLOAT_ARRAY) { array: FloatArray ->
                writeArrayNoMark(Float.SIZE_BYTES, array.size) { array.forEach { putFloat(it) } }
            },
            builtInOf(DOUBLE_ARRAY) { array: DoubleArray ->
                writeArrayNoMark(Double.SIZE_BYTES, array.size) { array.forEach { putDouble(it) } }
            },
            builtInOf(STRING) { s: String ->
                writeStringNoMark(s)
            },
            builtInOf(OBJECT_ARRAY) { objArray: Array<*> ->
                writeIntNoMark(objArray.size)
                objArray.forEach { write(it) }
            },
            builtInOf(LIST) { list: List<*> ->
                writeIntNoMark(list.size)
                list.forEach { write(it) }
            },
            builtInOf(ITERABLE) { iter: Iterable<*> ->
                iter.forEach { write(it) }
                writeFlag(END_OBJECT)
            },
            builtInOf(PAIR) { pair: Pair<*, *> ->
                write(pair.first)
                write(pair.second)
            },
            builtInOf(TRIPLE) { triple: Triple<*, *, *> ->
                write(triple.first)
                write(triple.second)
                write(triple.third)
            },
            builtInOf(MAP_ENTRY) { entry: Map.Entry<*, *> ->
                write(entry.key)
                write(entry.value)
            },
            builtInOf(MAP) { map: Map<*, *> ->
                writeIntNoMark(map.size)
                map.forEach { (key, value) ->
                    write(key)
                    write(value)
                }
            }
        )

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Any> builtInOf(
            flag: TypeFlag,
            noinline write: OutputSerializer.(T) -> Unit
        ): Pair<KClass<*>, BuiltInWriteHandle> =
            T::class to BuiltInWriteHandle(flag, write as WriteOperation)
    }
}

/**
 * Specifies the [flag] from where the given [write operation][lambda] originates from.
 */
private data class BuiltInWriteHandle(val flag: TypeFlag, val lambda: WriteOperation)