package io.github.pirocks.namedpipes

import java.io.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileStoreAttributeView


/**
 * Class represents named pipes.
 * @param deleteOnClose Whether or not to delete the created named pipe if pipe is closed.
 * @param openExistingFile If file already exists read from named pipe.
 * @param overWriteExistingFile If file already exists , delete and overwrite
 */
class NamedPipe(val namedPipe: File, overWriteExistingFile: Boolean = false, openExistingFile: Boolean = false, val deleteOnClose: Boolean = true) : Closeable{
    companion object {
        var mkfifoExecutableName = "mkfifo"
        class FileAlreadyExists : IllegalArgumentException("File already exists, not opening.")//todo maybe use kotlin stdlib class for this
        class ExistingFileNotANamedPipe : IllegalArgumentException("Existing file is not a named pipe")
        class NamedPipeCreationFailed : IllegalStateException("Creation of named pipe failed.")
    }

    init {
        windowsCheck()
        //critical section - begin
        //This critical section exists to prevent the creation of two named pipe objects unintentionally referring to the same pipe
        val lock = acquireLock()
        if(namedPipe.exists()){
            if(openExistingFile){
                val attrs = Files.readAttributes(namedPipe.toPath(), BasicFileAttributes::class.java)
                if(!attrs.isOther){
                    throw ExistingFileNotANamedPipe()
                }
            }else{
                throw FileAlreadyExists()
            }
        }
        if (!namedPipe.exists() || overWriteExistingFile) {
            if (overWriteExistingFile) {
                namedPipe.delete()
            }
            val creationRes = Runtime.getRuntime().exec(arrayOf(mkfifoExecutableName, namedPipe.absolutePath)).waitFor()
            if(creationRes != 0){
                throw NamedPipeCreationFailed()
            }
        }
        releaseLock(lock)
        //critical section - end
    }

    private fun acquireLock(): FileLock {
        val lockFile = Paths.get(File(namedPipe.absolutePath + ".lock").toPath().toUri())
        return RandomAccessFile(lockFile.toFile(),"rw").channel.lock()
//        open(lockFile, setOf(StandardOpenOption.CREATE)).lock()

    }

    private fun releaseLock(fileLock: FileLock){
        fileLock.release()
    }

    private fun windowsCheck() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            throw UnsupportedOperationException("This library only works with unix.")
        }
    }

    private var inputOpen = false


    private val inputStreamLazy: Lazy<DataInputStream> = lazy {
        val res = DataInputStream(FileInputStream(namedPipe))
        inputOpen = true
        res
    }
    private var outputOpen = false
    private val outputStreamLazy: Lazy<DataOutputStream> = lazy {
        val res = DataOutputStream(FileOutputStream(namedPipe))
        outputOpen = true
        res
    }
    public val writeStream by outputStreamLazy
    public val readStream by inputStreamLazy

    override fun close() {
        if (outputOpen) {
            outputStreamLazy.value.close()
            outputOpen = false
        }
        if (inputOpen) {
            inputStreamLazy.value.close()
            inputOpen = false
        }
        if (deleteOnClose) {
            namedPipe.delete()
        }
    }

    fun finalize() {
        close()
    }
/**
    override fun readFully(bytes: ByteArray?) {
        inputStreamLazy.value.readFully(bytes)
    }

    override fun readFully(bytes: ByteArray?, off: Int, len: Int) {
        inputStreamLazy.value.readFully(bytes, off, len)
    }

    override fun readInt(): Int {
        return inputStreamLazy.value.readInt()
    }

    override fun readUnsignedShort(): Int {
        return inputStreamLazy.value.readUnsignedShort()
    }

    override fun readUnsignedByte(): Int {
        return inputStreamLazy.value.readUnsignedByte()
    }

    override fun readUTF(): String {
        return inputStreamLazy.value.readUTF()
    }

    override fun readChar(): Char {
        return inputStreamLazy.value.readChar()
    }

    override fun readLine(): String {
        return inputStreamLazy.value.readLine()
    }

    override fun readByte(): Byte {
        return inputStreamLazy.value.readByte()
    }

    override fun readFloat(): Float {
        return inputStreamLazy.value.readFloat()
    }

    override fun skipBytes(n: Int): Int {
        return inputStreamLazy.value.skipBytes(n)
    }

    override fun readLong(): Long {
        return inputStreamLazy.value.readLong()
    }

    override fun readDouble(): Double {
        return inputStreamLazy.value.readDouble()
    }

    override fun readBoolean(): Boolean {
        return inputStreamLazy.value.readBoolean()
    }

    override fun readShort(): Short {
        return inputStreamLazy.value.readShort()
    }

    override fun writeShort(v: Int) {
        outputStreamLazy.value.writeShort(v)
    }

    override fun writeLong(v: Long) {
        outputStreamLazy.value.writeLong(v)
    }

    override fun writeDouble(v: Double) {
        outputStreamLazy.value.writeDouble(v)
    }

    override fun writeBytes(s: String?) {
        outputStreamLazy.value.writeBytes(s)
    }

    override fun writeByte(v: Int) {
        outputStreamLazy.value.writeByte(v)
    }

    override fun writeFloat(v: Float) {
        outputStreamLazy.value.writeFloat(v)
    }

    override fun write(b: Int) {
        outputStreamLazy.value.write(b)
    }

    override fun write(b: ByteArray?) {
        outputStreamLazy.value.write(b)
    }

    override fun write(b: ByteArray?, off: Int, lens: Int) {
        outputStreamLazy.value.write(b, off, lens)
    }

    override fun writeChars(s: String?) {
        outputStreamLazy.value.writeChars(s)
    }

    override fun writeChar(v: Int) {
        outputStreamLazy.value.writeChar(v)
    }

    override fun writeBoolean(v: Boolean) {
        outputStreamLazy.value.writeBoolean(v)
    }

    override fun writeUTF(str: String?) {
        outputStreamLazy.value.writeUTF(str)
    }

    override fun writeInt(v: Int) {
        outputStreamLazy.value.writeInt(v)
    }
    */
}
