package io.github.pirocks.namedpipes

import java.io.*
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException

/**
 * Class represents named pipes.
 * @param deleteOnClose Whether or not to delete the created named pipe if pipe is closed.
 * @param openExistingFile If file already exists read from named pipe.
 * @param overWriteExistingFile If file already exists , delete and overwrite
 */
class NamedPipe(val name: File, overWriteExistingFile: Boolean = false, openExistingFile: Boolean = false, val deleteOnClose: Boolean = true) : DataInput, DataOutput, Closeable {
    companion object {
        var mkfifoExecutableName = "mkfifo"
    }

    init {
        if (System.getProperty("os.name").startsWith("Windows")) {
            throw UnsupportedOperationException("This library only works with unix.")
        }
        if (!name.exists() || overWriteExistingFile || openExistingFile) {
            if (overWriteExistingFile) {
                name.delete()
            }
            Runtime.getRuntime().exec(arrayOf(mkfifoExecutableName, name.absolutePath))
        } else {
            throw IllegalArgumentException("File already existed")
        }
    }

    private var inputOpen = false


    private val inputStream: Lazy<DataInputStream> = lazy {
        val res = DataInputStream(FileInputStream(name))
        inputOpen = true
        res
    }
    private var outputOpen = false
    private val outputStream: Lazy<DataOutputStream> = lazy {
        val res = DataOutputStream(FileOutputStream(name))
        outputOpen = true
        res
    }

    override fun close() {
        if (outputOpen) {
            outputStream.value.close()
            outputOpen = false
        }
        if (inputOpen) {
            inputStream.value.close()
            inputOpen = false
        }
        if (deleteOnClose) {
            name.delete()
        }
    }

    fun finalize() {
        close()
    }

    override fun readFully(bytes: ByteArray?) {
        inputStream.value.readFully(bytes)
    }

    override fun readFully(bytes: ByteArray?, off: Int, len: Int) {
        inputStream.value.readFully(bytes, off, len)
    }

    override fun readInt(): Int {
        return inputStream.value.readInt()
    }

    override fun readUnsignedShort(): Int {
        return inputStream.value.readUnsignedShort()
    }

    override fun readUnsignedByte(): Int {
        return inputStream.value.readUnsignedByte()
    }

    override fun readUTF(): String {
        return inputStream.value.readUTF()
    }

    override fun readChar(): Char {
        return inputStream.value.readChar()
    }

    override fun readLine(): String {
        return inputStream.value.readLine()
    }

    override fun readByte(): Byte {
        return inputStream.value.readByte()
    }

    override fun readFloat(): Float {
        return inputStream.value.readFloat()
    }

    override fun skipBytes(n: Int): Int {
        return inputStream.value.skipBytes(n)
    }

    override fun readLong(): Long {
        return inputStream.value.readLong()
    }

    override fun readDouble(): Double {
        return inputStream.value.readDouble()
    }

    override fun readBoolean(): Boolean {
        return inputStream.value.readBoolean()
    }

    override fun readShort(): Short {
        return inputStream.value.readShort()
    }

    override fun writeShort(v: Int) {
        outputStream.value.writeShort(v)
    }

    override fun writeLong(v: Long) {
        outputStream.value.writeLong(v)
    }

    override fun writeDouble(v: Double) {
        outputStream.value.writeDouble(v)
    }

    override fun writeBytes(s: String?) {
        outputStream.value.writeBytes(s)
    }

    override fun writeByte(v: Int) {
        outputStream.value.writeByte(v)
    }

    override fun writeFloat(v: Float) {
        outputStream.value.writeFloat(v)
    }

    override fun write(b: Int) {
        outputStream.value.write(b)
    }

    override fun write(b: ByteArray?) {
        outputStream.value.write(b)
    }

    override fun write(b: ByteArray?, off: Int, lens: Int) {
        outputStream.value.write(b, off, lens)
    }

    override fun writeChars(s: String?) {
        outputStream.value.writeChars(s)
    }

    override fun writeChar(v: Int) {
        outputStream.value.writeChar(v)
    }

    override fun writeBoolean(v: Boolean) {
        outputStream.value.writeBoolean(v)
    }

    override fun writeUTF(str: String?) {
        outputStream.value.writeUTF(str)
    }

    override fun writeInt(v: Int) {
        outputStream.value.writeInt(v)
    }
}
