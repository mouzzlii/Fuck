package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class BackupCryptoTest {
    private val backup = """{"format":"TRAKUNA_BACKUP","schemaVersion":2,"recordCount":1,"trackingNumber":"TEST123456","recipient":"Test Empfaenger"}"""

    @Test
    fun encryptedBackupRoundTripsWithoutExposingPlaintext() {
        val exportPassword = "Sicheres Passwort 2026!".toCharArray()
        val encrypted = BackupCrypto.encrypt(backup, exportPassword)
        assertTrue(exportPassword.all { it == '\u0000' })
        assertTrue(BackupCrypto.isEncryptedBackup(encrypted))
        assertFalse(encrypted.contains("TEST123456"))
        assertFalse(encrypted.contains("Test Empfaenger"))
        val restorePassword = "Sicheres Passwort 2026!".toCharArray()
        assertEquals(backup, BackupCrypto.decrypt(encrypted, restorePassword))
        assertTrue(restorePassword.all { it == '\u0000' })
    }

    @Test
    fun wrongPasswordAndTamperedCiphertextAreRejected() {
        val encrypted = BackupCrypto.encrypt(backup, "Sicheres Passwort 2026!".toCharArray())
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(encrypted, "Anderes Passwort 2026!".toCharArray())
        }
        val tampered = encrypted.dropLast(4) + "AAAA"
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(tampered, "Sicheres Passwort 2026!".toCharArray())
        }
    }

    @Test
    fun weakPasswordsAndOversizedPayloadsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.encrypt(backup, "short".toCharArray())
        }
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.encrypt("x".repeat(BackupCrypto.MAX_ENCRYPTED_BACKUP_BYTES), "Sicheres Passwort 2026!".toCharArray())
        }
    }

    @Test
    fun everyExportUsesFreshSaltAndNonceAndAuthenticatesHeader() {
        val password = "Sicheres Passwort 2026!"
        val first = BackupCrypto.encrypt(backup, password.toCharArray())
        val second = BackupCrypto.encrypt(backup, password.toCharArray())
        assertNotEquals(first, second)

        val prefix = "TRAKUNA-ENCRYPTED-BACKUP-V1\n"
        val payload = Base64.getDecoder().decode(first.substring(prefix.length))
        payload[5] = (payload[5].toInt() xor 0x01).toByte()
        val alteredHeader = prefix + Base64.getEncoder().encodeToString(payload)
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(alteredHeader, password.toCharArray())
        }
    }

    @Test
    fun pbkdf2UsesStandardHmacSha256AndExplicitUtf8Encoding() {
        val ascii = BackupCrypto.deriveKey("password".toCharArray(), "salt".toByteArray(), 1)
        assertEquals(
            "120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b",
            ascii.joinToString("") { "%02x".format(it) }
        )

        val unicode = BackupCrypto.deriveKey("pässwörd".toCharArray(), "trakuna-test-salt".toByteArray(), 2)
        assertEquals(
            "ea3edbe2b3d46cc1cc1b06ded1a2f8503eb4e40cac966029128f172eb7c0ba04",
            unicode.joinToString("") { "%02x".format(it) }
        )
    }

    @Test
    fun unicodePassphraseCanRestoreBackup() {
        val password = "Grüße aus Österreich € 🔐"
        val encrypted = BackupCrypto.encrypt(backup, password.toCharArray())
        assertEquals(backup, BackupCrypto.decrypt(encrypted, password.toCharArray()))
    }

    @Test
    fun legacyJsonIsNotMistakenForAnEncryptedBackup() {
        assertFalse(BackupCrypto.isEncryptedBackup(backup))
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(backup, "Sicheres Passwort 2026!".toCharArray())
        }
    }
}
