package de.trakuna.app

import java.io.ByteArrayInputStream
import java.io.InputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupIoTest {
    @Test
    fun backupImportLimitKeepsFourMibOfHeadroomForPortableJsonBackups() {
        assertEquals(4 * 1024 * 1024, ShipmentStore.MAX_BACKUP_BYTES)
    }

    @Test
    fun readsUtf8WithinLimit() {
        val value = "TRAKUNA-Sicherung – Österreich"
        assertEquals(value, readBoundedUtf8(ByteArrayInputStream(value.toByteArray(Charsets.UTF_8)), 128))
    }

    @Test
    fun acceptsInputExactlyAtByteLimitIncludingMultibyteCharacters() {
        val value = "Ö"
        val bytes = value.toByteArray(Charsets.UTF_8)
        assertEquals(2, bytes.size)
        assertEquals(value, readBoundedUtf8(ByteArrayInputStream(bytes), bytes.size))
    }

    @Test
    fun acceptsEmptyInputOnlyWhenZeroByteLimitAllowsIt() {
        assertEquals("", readBoundedUtf8(ByteArrayInputStream(byteArrayOf()), 0))
        assertThrows(IllegalArgumentException::class.java) {
            readBoundedUtf8(ByteArrayInputStream(byteArrayOf(0x41)), 0)
        }
    }

    @Test
    fun rejectsInputLargerThanLimit() {
        val input = ByteArrayInputStream("12345".toByteArray(Charsets.UTF_8))
        assertThrows(IllegalArgumentException::class.java) { readBoundedUtf8(input, 4) }
    }

    @Test
    fun rejectsMalformedUtf8InsteadOfSilentlyReplacingBytes() {
        val malformed = byteArrayOf(0x7B, 0x22, 0xFF.toByte(), 0x22, 0x7D)
        assertThrows(java.nio.charset.CharacterCodingException::class.java) {
            readBoundedUtf8(ByteArrayInputStream(malformed), 32)
        }
    }

    @Test
    fun handlesStreamsThatTemporarilyReturnZeroBytes() {
        val bytes = "Backup".toByteArray(Charsets.UTF_8)
        val input = object : InputStream() {
            private val delegate = ByteArrayInputStream(bytes)
            private var returnZero = true
            override fun read(): Int = delegate.read()
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                if (returnZero) {
                    returnZero = false
                    return 0
                }
                return delegate.read(buffer, offset, length)
            }
        }
        assertEquals("Backup", readBoundedUtf8(input, 16))
    }

    @Test
    fun strictUtf8DecoderRejectsMalformedAndUnmappableByteSequences() {
        assertEquals("TRAKUNA – Österreich", decodeStrictUtf8("TRAKUNA – Österreich".toByteArray(Charsets.UTF_8)))
        assertThrows(java.nio.charset.CharacterCodingException::class.java) {
            decodeStrictUtf8(byteArrayOf(0xC3.toByte(), 0x28))
        }
    }

    @Test
    fun rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            readBoundedUtf8(ByteArrayInputStream(byteArrayOf()), -1)
        }
    }
}
