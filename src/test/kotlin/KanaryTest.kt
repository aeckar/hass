import kanary.binary
import kanary.protocol
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.test.assertEquals

data class MyClass(val value: Int) {
    companion object {
        init {
            protocol <MyClass> {
                read {
                    MyClass(readInt())
                }
                write {
                    writeInt(it.value)
                }
            }
        }
    }
}
class Test {
    @Test
    fun test() {
        FileOutputStream("cache.bin").binary().use {
            it.write(MyClass(2))
        }
        val read = FileInputStream("cache.bin").binary().use {
            it.read<MyClass>()
        }
        assertEquals(2, read.value)
    }
}