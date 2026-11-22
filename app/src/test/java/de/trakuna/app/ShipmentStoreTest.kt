package de.trakuna.app

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ShipmentStoreTest {
    private lateinit var context: Context
    private lateinit var store: ShipmentStore

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("trakuna_shipments", Context.MODE_PRIVATE).edit().clear().commit()
        store = ShipmentStore(context)
    }

    @Test
    fun addsAndListsIncomingShipment() {
        val result = store.add("1Z999AA10123456784", "UPS", ShipmentFlow.INCOMING_FOR_CUSTOMER, "Testkunde", "Vorsichtig")
        assertTrue(result is ShipmentUpdate.Saved)
        val saved = (result as ShipmentUpdate.Saved).record
        assertEquals(ShipmentStatus.RECEIVED_BY_SHOP, saved.status)
        assertEquals("Testkunde", store.all().single().recipient)
        assertEquals("Vorsichtig", store.all().single().note)
    }

    @Test
    fun preventsDuplicateActiveShipmentForSameFlow() {
        store.add("1Z999AA10123456784", "UPS", ShipmentFlow.INCOMING_FOR_CUSTOMER)
        store.add("1Z999AA10123456784", "UPS", ShipmentFlow.INCOMING_FOR_CUSTOMER)
        assertEquals(1, store.all().size)
    }

    @Test
    fun incomingShipmentFollowsPickupLifecycle() {
        val record = (store.add("1Z999AA10123456784", "UPS", ShipmentFlow.INCOMING_FOR_CUSTOMER) as ShipmentUpdate.Saved).record
        val ready = (store.advance(record.id) as ShipmentUpdate.Saved).record
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, ready.status)
        val handedOver = (store.advance(record.id) as ShipmentUpdate.Saved).record
        assertEquals(ShipmentStatus.HANDED_TO_CUSTOMER, handedOver.status)
        assertTrue(store.advance(record.id) is ShipmentUpdate.Rejected)
    }

    @Test
    fun rejectsInvalidTrackingNumbers() {
        assertTrue(store.add("ABC", null, ShipmentFlow.CUSTOMER_RETURN) is ShipmentUpdate.Rejected)
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun exportsCsvAndRemovesRecords() {
        val record = (store.add("1Z999AA10123456784", "UPS", ShipmentFlow.OUTGOING_FROM_CUSTOMER) as ShipmentUpdate.Saved).record
        val csv = store.exportCsv()
        assertTrue(csv.contains("Sendungsnummer"))
        assertTrue(csv.contains("1Z999AA10123456784"))
        assertTrue(store.remove(record.id))
        assertFalse(store.remove(record.id))
        assertTrue(store.all().isEmpty())
    }
}
