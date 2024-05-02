package kanary

import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.assertEquals

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
    val objValue: Message
) {
    private companion object {
        init {
            protocol<Data> {
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
                        read()
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
                }
            }
        }
    }
}

data class Message(val message: String) {
    private companion object {
        init {
            protocol<Message> {
                read = {
                    Message(readString())
                }
                write = {
                    write(it.message)
                }
            }
        }
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
            Message("Hello, Copilot!")
        )
        val writeMessage = Message("Goodbye, world!")
        FileOutputStream("src/test/resources/cache.bin").binary().use {
            it.write(writeData)
            it.write(writeMessage)
        }
        val readData: Data
        val readMessage: Message
        FileInputStream("src/test/resources/cache.bin").binary().use {
            readData = it.read<Data>()
            readMessage = it.read<Message>()
        }
        assertEquals(writeData, readData)
        assertEquals(writeMessage, readMessage)
    }
}