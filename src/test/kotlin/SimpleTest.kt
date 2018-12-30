import io.github.pirocks.namedpipes.NamedPipe
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files

class SimpleTest{
    @Test
    fun simple(){
        val tempFile = Files.createTempFile("named-pipes-test", "").toFile()
        tempFile.delete()
        val pipe = NamedPipe(tempFile, openExistingFile = false,overWriteExistingFile = false)
        val toWrite = 2739847
        val writeThread = Thread {
            pipe.writeStream.writeInt(toWrite)
        }
        writeThread.start()
        var intWasRead = false
        val readThread = Thread {
            Assert.assertEquals(pipe.readStream.readInt(), toWrite)
            intWasRead = true
        }
        readThread.start()
        writeThread.join()
        readThread.join()
        Assert.assertTrue(intWasRead)
    }
}