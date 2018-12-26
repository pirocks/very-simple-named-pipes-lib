import io.github.pirocks.namedpipes.NamedPipe
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files

class SimpleTest{
    @Test
    fun simple(){
        val tempFile = Files.createTempFile("named-pipes-test", "").toFile()
        val pipe = NamedPipe(tempFile, openExistingFile = true)
        val toWrite = 2739847
        pipe.writeInt(toWrite)
        Assert.assertEquals(pipe.readInt(),toWrite)
    }
}