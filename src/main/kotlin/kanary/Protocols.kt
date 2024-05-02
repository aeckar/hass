package kanary

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream

typealias ReadOperation<T> = BinaryInput.() -> T
typealias WriteOperation<T> = BinaryOutput.(T) -> Unit

fun InputStream.binary() = BinaryInput(this)

fun OutputStream.binary() = BinaryOutput(this)

class BinaryInput internal constructor(private val stream: InputStream) : Closeable {
    private val intBytesBuffer = ByteArray(Int.SIZE_BYTES)

    fun readBoolean() = stream.read() == 1
    fun readByte() = stream.read()
    fun readChar() = ((stream.read() shl Byte.SIZE_BITS) or stream.read()).toChar()
    fun readInt() = intBytesBuffer.apply { stream.read(this) }.toInt()
    fun readString() = String(stream.readNBytes(stream.read())) // size, followed by literal

    inline fun <reified T> read(): T {
        val className = T::class.qualifiedName
        val index = Protocols.classNames.indexOf(className)
        if (index == -1) {
            throw IllegalArgumentException("Class '$className' does not have a binary I/O protocol")
        }
        return Protocols.readOps[index](this) as T
    }

    override fun close() {
        stream.close()
    }

    private companion object {
        fun ByteArray.toInt(): Int {
            var result = 0
            forEachIndexed { index, byte ->
                result = result or (byte.toUByte().toInt() shl (lastIndex - index))
            }
            return result
        }
    }
}

@JvmInline
value class BinaryOutput internal constructor(private val stream: OutputStream) : Closeable {
    fun writeBoolean(cond: Boolean) {
        stream.write(if (cond) 1 else 0)
    }

    fun writeByte(b: Byte) {
        stream.write(b.toInt())
    }

    fun writeChar(c: Char) {
        val code = c.code
        stream.write(code ushr Char.SIZE_BITS)
        stream.write(code.toByte().toInt())
    }

    fun writeInt(n: Int) {
        stream.write(n.toByteArray())
    }
    fun writeString(s: String) {
        stream.write(s.length.toByteArray())
        stream.write(s.toByteArray(Charsets.UTF_8))
    }

    inline fun <reified T> write(obj: T) {
        val className = T::class.qualifiedName
        val index = Protocols.classNames.indexOf(className)
        if (index == -1) {
            throw IllegalArgumentException("Class '$className' does not have a binary I/O protocol")
        }
        Protocols.writeOps[index](obj)
    }

    override fun close() {
        stream.close()
    }

    private companion object {
        fun Int.toByteArray(): ByteArray {
            val size = Int.SIZE_BYTES
            val last = size - 1
            return ByteArray(size).apply {
                repeat(size) {
                    this[it] = (this@toByteArray ushr (last - it)).toByte()
                }
            }
        }
    }
}

class BinaryProtocolBuilderScope<T> {
    var onRead: ReadOperation<T> by AssignOnce()
    var onWrite: WriteOperation<T> by AssignOnce()

    fun read(onRead: ReadOperation<T>) {
        this.onRead = onRead
    }

    fun write(onWrite: WriteOperation<T>) {
        this.onWrite = onWrite
    }
}

@PublishedApi
internal object Protocols {
    val classNames = mutableListOf<String>()
    val readOps = mutableListOf<ReadOperation<*>>()
    val writeOps = mutableListOf<WriteOperation<Any?>>()

    @Suppress("UNCHECKED_CAST")
    fun add(className: String, builderScope: BinaryProtocolBuilderScope<*>) {
        classNames += className
        readOps += builderScope.onRead
        writeOps += builderScope.onWrite as WriteOperation<Any?>
    }
}

/**
 * TODO describe
 */
inline fun <reified T> protocol(builder: BinaryProtocolBuilderScope<T>.() -> Unit) {
    val builderScope = BinaryProtocolBuilderScope<T>()
    builder(builderScope)
    synchronized(Protocols) {
        try {
            Protocols.add(T::class.qualifiedName!!, builderScope)
        } catch (_: NullPointerException) {
            throw IllegalArgumentException("Binary I/O only supported for top-level classes")
        }
    }
}
