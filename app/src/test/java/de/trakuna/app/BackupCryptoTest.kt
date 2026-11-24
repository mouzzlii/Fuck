package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {
    private val backup = """{"format":"TRAKUNA_BACKUP","schemaVersion":2,"recordCount":1,"trackingNumber":"TEST123456","recipient":"Test Empfaenger"}"""

    @Test
    fun encryptedBackupRoundTripsWithoutExposingPlaintext() {
        val encrypted = BackupCrypto.encrypt(backup, "Sicheres Passwort 2026!".toCharArray())
        assertTrue(BackupCrypto.isEncryptedBackup(encrypted))
        assertFalse(encrypted.contains("TEST123456"))
        assertFalse(encrypted.contains("Test Empfaenger"))
        assertEquals(backup, BackupCrypto.decrypt(encrypted, "Sicheres Passwort 2026!".toCharArray()))
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
    fun legacyJsonIsNotMistakenForAnEncryptedBackup() {
        assertFalse(BackupCrypto.isEncryptedBackup(backup))
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.decrypt(backup, "Sicheres Passwort 2026!".toCharArray())
        }
    }
}
