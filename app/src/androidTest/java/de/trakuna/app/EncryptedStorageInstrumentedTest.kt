package de.trakuna.app

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedStorageInstrumentedTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearPreferences() {
        clearStorePreferences()
    }

    @After
    fun removeTestData() {
        clearStorePreferences()
    }

    @Test
    fun shipmentRecordsAreEncryptedAndSurviveStoreRecreation() {
        val store = ShipmentStore(context)
        val result = store.add(
            rawTrackingNumber = "TEST123456",
            carrier = "DPD",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            recipient = "Test Empfaenger",
            note = "Instrumentierungstest"
        )
        assertTrue(result is ShipmentUpdate.Saved)
        val saved = (result as ShipmentUpdate.Saved).record
        val backup = store.exportBackupJson()
        assertTrue(backup.toByteArray(Charsets.UTF_8).size <= ShipmentStore.MAX_BACKUP_BYTES)
        assertTrue(store.validateBackup(backup) is BackupValidation.Valid)

        val preferences = context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE)
        val persistedText = preferences.all.values.filterIsInstance<String>().joinToString("|")
        assertFalse(persistedText.contains("TEST123456"))
        assertFalse(persistedText.contains("Test Empfaenger"))
        assertFalse(persistedText.contains("Instrumentierungstest"))

        val reopened = ShipmentStore(context)
        assertEquals(listOf(saved), reopened.all())
        assertEquals(null, reopened.storageError())
    }

    @Test
    fun repeatedStatusConfirmationCannotAdvanceTheSameShipmentTwice() {
        val store = ShipmentStore(context)
        val added = store.add(
            rawTrackingNumber = "DOUBLE12345",
            carrier = "DPD",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER
        )
        assertTrue(added is ShipmentUpdate.Saved)
        val original = (added as ShipmentUpdate.Saved).record

        val first = store.advance(original.id, expectedStatus = original.status)
        assertTrue(first is ShipmentUpdate.Saved)
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, store.all().single().status)

        // Simulate a second tap from the still-open confirmation dialog.
        val duplicate = store.advance(original.id, expectedStatus = original.status)
        assertTrue(duplicate is ShipmentUpdate.Rejected)
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, store.all().single().status)
    }

    @Test
    fun damagedEncryptedShipmentStorageFailsClosedInsteadOfBecomingAnEmptyWritableStore() {
        val store = ShipmentStore(context)
        assertTrue(
            store.add(
                rawTrackingNumber = "TEST123456",
                carrier = "GLS",
                flow = ShipmentFlow.INCOMING_FOR_CUSTOMER
            ) is ShipmentUpdate.Saved
        )

        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE)
            .edit()
            .putString("records_encrypted_v2", "%%%")
            .commit()

        val reopened = ShipmentStore(context)
        assertTrue(reopened.all().isEmpty())
        assertNotNull(reopened.storageError())
        assertTrue(
            reopened.add(
                rawTrackingNumber = "NEXT123456",
                carrier = "GLS",
                flow = ShipmentFlow.INCOMING_FOR_CUSTOMER
            ) is ShipmentUpdate.Rejected
        )
    }

    @Test
    fun partnerHandoffCacheIsEncryptedAndSurvivesCacheRecreation() {
        val cache = SecureHandoffCache(context)
        assertTrue(cache.save("TEST123456", "Hermes"))

        val preferences = context.getSharedPreferences("trakuna_handoff", Context.MODE_PRIVATE)
        val persistedText = preferences.all.values.filterIsInstance<String>().joinToString("|")
        assertFalse(persistedText.contains("TEST123456"))
        assertFalse(persistedText.contains("Hermes"))

        assertEquals("TEST123456", SecureHandoffCache(context).trackingNumber())
    }

    @Test
    fun damagedPartnerHandoffCacheFailsClosedInsteadOfReturningAnEmptyNumber() {
        val cache = SecureHandoffCache(context)
        assertTrue(cache.save("TEST123456", "Hermes"))

        context.getSharedPreferences("trakuna_handoff", Context.MODE_PRIVATE)
            .edit()
            .putString("tracking_number_encrypted_v1", "%%%")
            .commit()

        val reopened = SecureHandoffCache(context)
        assertNotNull(reopened.failureReason())
        val read = runCatching { reopened.trackingNumber() }
        assertTrue("Beschädigter Übergabe-Cache darf nicht als leerer Wert gelten.", read.isFailure)
    }

    @Test
    fun validBackupCanRecoverDamagedEncryptedShipmentStorage() {
        val original = ShipmentStore(context)
        val saved = original.add(
            rawTrackingNumber = "TEST123456",
            carrier = "DPD",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            recipient = "Test Empfaenger",
            note = "Wiederherstellungstest"
        )
        assertTrue(saved is ShipmentUpdate.Saved)
        val password = "Android Backup Password 2026!"
        val backup = BackupCrypto.encrypt(original.exportBackupJson(), password.toCharArray())

        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE)
            .edit()
            .putString("records_encrypted_v2", "%%%")
            .commit()

        val damaged = ShipmentStore(context)
        assertNotNull(damaged.storageError())
        val decryptedBackup = BackupCrypto.decrypt(backup, password.toCharArray())
        val restored = damaged.restoreBackup(decryptedBackup)
        assertEquals(BackupRestoreResult.Restored(1), restored)
        assertEquals(listOf((saved as ShipmentUpdate.Saved).record), damaged.all())
        assertEquals(null, damaged.storageError())
    }




    @Test
    fun passwordEncryptedBackupUsesAndroidCryptoProviderAndRejectsWrongPassword() {
        val store = ShipmentStore(context)
        val saved = store.add(
            rawTrackingNumber = "ANDROIDTEST123",
            carrier = "DPD",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            recipient = "Android Test",
            note = "Backup Crypto Instrumentierung"
        )
        assertTrue(saved is ShipmentUpdate.Saved)
        val plain = store.exportBackupJson()
        val password = "Android Backup Password 2026!"
        val encrypted = BackupCrypto.encrypt(plain, password.toCharArray())
        assertTrue(BackupCrypto.isEncryptedBackup(encrypted))
        assertFalse(encrypted.contains("ANDROIDTEST123"))
        val decrypted = BackupCrypto.decrypt(encrypted, password.toCharArray())
        assertEquals(plain, decrypted)
        assertTrue(store.validateBackup(decrypted) is BackupValidation.Valid)
        assertTrue(runCatching {
            BackupCrypto.decrypt(encrypted, "Wrong Password 2026!".toCharArray())
        }.isFailure)
    }


    @Test
    fun legacy210000IterationBackupUsesTheOriginalAndroidPbkdf2Provider() {
        val plain = """{"format":"TRAKUNA_BACKUP","schemaVersion":2,"recordCount":0}"""
        val password = "Grüße aus Österreich € 2026!"
        val passwordChars = password.toCharArray()
        val salt = ByteArray(16) { it.toByte() }
        val nonce = ByteArray(12) { (it + 16).toByte() }
        val prefix = "TRAKUNA-ENCRYPTED-BACKUP-V1\n"
        val header = java.nio.ByteBuffer.allocate(33)
            .put(1.toByte())
            .putInt(210_000)
            .put(salt)
            .put(nonce)
            .array()
        val spec = javax.crypto.spec.PBEKeySpec(passwordChars, salt, 210_000, 256)
        val key = try {
            javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            passwordChars.fill('\u0000')
        }
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            javax.crypto.Cipher.ENCRYPT_MODE,
            javax.crypto.spec.SecretKeySpec(key, "AES"),
            javax.crypto.spec.GCMParameterSpec(128, nonce)
        )
        cipher.updateAAD(prefix.toByteArray(Charsets.US_ASCII))
        cipher.updateAAD(header)
        val encrypted = prefix + java.util.Base64.getEncoder().encodeToString(
            header + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        )
        java.util.Arrays.fill(key, 0)

        assertEquals(plain, BackupCrypto.decrypt(encrypted, password.toCharArray()))
    }


    private fun clearStorePreferences() {
        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("trakuna_handoff", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
