package kanary

import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.assertEquals

val dataProtocol = protocolOf<Data> {
    read = {
        Data(
            readBoolean(),
            readByte(),
            readChar(),
            readShort(),
            readInt(),
            readLong(),
            readFloat(),
            readDouble(),
            readString(),
            readObject(),
            readNullablesArray(),
            readNullablesList(),
            readNullablesIterable()
        )
    }
    write = {
        write(it.booleanValue)
        write(it.byteValue)
        write(it.charValue)
        write(it.shortValue)
        write(it.intValue)
        write(it.longValue)
        write(it.floatValue)
        write(it.doubleValue)
        write(it.stringValue)
        write(it.objValue)
        writeAllOr(it.array)
        writeAllOr(it.list)
        writeAllOr(it.iterable)
    }
}

val messageProtocol = protocolOf<Message> {
    read = {
        Message(readString())
    }
    write = {
        write(it.message)
    }
}

data class Data(
    val booleanValue: Boolean,
    val byteValue: Byte,
    val charValue: Char,
    val shortValue: Short,
    val intValue: Int,
    val longValue: Long,
    val floatValue: Float,
    val doubleValue: Double,
    val stringValue: String,
    val objValue: Message,
    val array: Array<out Any?>,
    val list: List<Any?>,
    val iterable: Iterable<Any?>
) {
    private companion object {
        init { dataProtocol }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Data

        if (booleanValue != other.booleanValue) return false
        if (byteValue != other.byteValue) return false
        if (charValue != other.charValue) return false
        if (shortValue != other.shortValue) return false
        if (intValue != other.intValue) return false
        if (longValue != other.longValue) return false
        if (floatValue != other.floatValue) return false
        if (doubleValue != other.doubleValue) return false
        if (stringValue != other.stringValue) return false
        if (objValue != other.objValue) return false
        if (!array.contentEquals(other.array)) return false
        if (list != other.list) return false
        if (iterable.toList() != other.iterable.toList()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = booleanValue.hashCode()
        result = 31 * result + byteValue
        result = 31 * result + charValue.hashCode()
        result = 31 * result + shortValue
        result = 31 * result + intValue
        result = 31 * result + longValue.hashCode()
        result = 31 * result + floatValue.hashCode()
        result = 31 * result + doubleValue.hashCode()
        result = 31 * result + stringValue.hashCode()
        result = 31 * result + objValue.hashCode()
        result = 31 * result + array.contentHashCode()
        result = 31 * result + list.hashCode()
        result = 31 * result + iterable.hashCode()
        return result
    }
}

data class Message(val message: String) {
    private companion object {
        init { messageProtocol }
    }
}

class KanaryTest {
    @Test
    fun kanaryTest() {
        val writeData = Data(
            true,
            42,
            'X',
            -17,
            123,
            987654321L,
            -3.14159f,
            2.71828,
            "Random string",
            Message("Hello, Copilot!"),
            arrayOf("apple", "banana", "cherry", "date", "elderberry"),
            listOf<String>(),
            setOf(10, 20, 30, 40, 50)
        )
        val writeMessage = Message("Goodbye, world!")
        FileOutputStream("src/test/resources/cache.bin").binary().use {
            it.write(writeData)
            it.write(writeMessage)
        }
        val readData: Data
        val readMessage: Message
        FileInputStream("src/test/resources/cache.bin").binary().use {
            readData = it.readObject<Data>()
            readMessage = it.readObject<Message>()
        }
        assertEquals(writeData, readData)
        assertEquals(writeMessage, readMessage)
    }
}