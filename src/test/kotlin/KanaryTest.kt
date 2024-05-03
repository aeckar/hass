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
            readPolymorphic()
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
        write(it.arbitraryObj)
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
    val arbitraryObj: Any
) {
    private companion object {
        init { dataProtocol }
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
            mutableListOf("Hi!", "Hola!", "Bonjour!")
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