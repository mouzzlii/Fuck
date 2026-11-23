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
        val backup = original.exportBackupJson()

        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE)
            .edit()
            .putString("records_encrypted_v2", "%%%")
            .commit()

        val damaged = ShipmentStore(context)
        assertNotNull(damaged.storageError())
        val restored = damaged.restoreBackup(backup)
        assertEquals(BackupRestoreResult.Restored(1), restored)
        assertEquals(listOf((saved as ShipmentUpdate.Saved).record), damaged.all())
        assertEquals(null, damaged.storageError())
    }

    private fun clearStorePreferences() {
        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("trakuna_handoff", Context.MODE_PRIVATE).edit().clear().commit()
    }
}
