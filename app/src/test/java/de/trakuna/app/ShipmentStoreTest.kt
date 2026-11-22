package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShipmentStoreTest {
    @Test
    fun normalizesAndValidatesTrackingNumbers() {
        assertEquals("1Z999AA10123456784", ShipmentRules.normalizeTrackingNumber(" 1Z 999AA10123456784 "))
        assertTrue(ShipmentRules.isValidTrackingNumber("1Z999AA10123456784"))
        assertFalse(ShipmentRules.isValidTrackingNumber("ABC"))
        assertFalse(ShipmentRules.isValidTrackingNumber("123456!"))
        assertFalse(ShipmentRules.isValidTrackingNumber("12345678901234567890123456789012345678901"))
    }

    @Test
    fun incomingShipmentFollowsPickupLifecycle() {
        assertEquals(
            ShipmentStatus.RECEIVED_BY_SHOP,
            ShipmentRules.initialStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER)
        )
        assertEquals(
            ShipmentStatus.READY_FOR_PICKUP,
            ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.RECEIVED_BY_SHOP)
        )
        assertEquals(
            ShipmentStatus.HANDED_TO_CUSTOMER,
            ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.READY_FOR_PICKUP)
        )
        assertEquals(
            null,
            ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.HANDED_TO_CUSTOMER)
        )
    }

    @Test
    fun outgoingAndReturnFlowsFinishAtCarrierHandover() {
        assertEquals(
            ShipmentStatus.HANDED_TO_CARRIER,
            ShipmentRules.nextStatus(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.ACCEPTED_FROM_CUSTOMER)
        )
        assertEquals(
            ShipmentStatus.HANDED_TO_CARRIER,
            ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.RETURN_ACCEPTED)
        )
        assertEquals(
            null,
            ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.HANDED_TO_CARRIER)
        )
        assertEquals(
            null,
            ShipmentRules.nextStatus(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.RETURN_ACCEPTED)
        )
        assertEquals(
            null,
            ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.ACCEPTED_FROM_CUSTOMER)
        )
    }

    @Test
    fun shipmentStatusesMustMatchTheirWorkflow() {
        assertTrue(ShipmentRules.isStatusValidForFlow(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.READY_FOR_PICKUP))
        assertTrue(ShipmentRules.isStatusValidForFlow(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.ACCEPTED_FROM_CUSTOMER))
        assertTrue(ShipmentRules.isStatusValidForFlow(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.RETURN_ACCEPTED))
        assertFalse(ShipmentRules.isStatusValidForFlow(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.RETURN_ACCEPTED))
        assertFalse(ShipmentRules.isStatusValidForFlow(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.READY_FOR_PICKUP))
    }
}
