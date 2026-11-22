package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShipmentStoreTest {
    @Test
    fun recordValidationRejectsInvalidWorkflowAndTimestamps() {
        val valid = ShipmentRecord(
            id = "record-1",
            trackingNumber = "ABC1234567",
            carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.RECEIVED_BY_SHOP,
            createdAt = 100L,
            updatedAt = 100L,
            recipient = "Max Mustermann",
            note = "Paket liegt im Regal"
        )
        assertTrue(ShipmentRules.isValidRecord(valid))
        assertFalse(ShipmentRules.isValidRecord(valid.copy(status = ShipmentStatus.RETURN_ACCEPTED)))
        assertFalse(ShipmentRules.isValidRecord(valid.copy(updatedAt = 99L)))
        assertFalse(ShipmentRules.isValidRecord(valid.copy(id = "")))
        assertFalse(ShipmentRules.isValidRecord(valid.copy(trackingNumber = "ABC")))
        assertFalse(ShipmentRules.isValidRecord(valid.copy(note = "x".repeat(501))))
    }

    @Test
    fun csvExportEscapesSpreadsheetFormulasAndQuotes() {
        assertEquals("\"'=1+1\"", ShipmentRules.csvSafeCell("=1+1"))
        assertEquals("\"'  @SUM(A1:A2)\"", ShipmentRules.csvSafeCell("  @SUM(A1:A2)"))
        assertEquals("\"normal text\"", ShipmentRules.csvSafeCell("normal text"))
        assertEquals("\"say \"\"hello\"\"\"", ShipmentRules.csvSafeCell("say \"hello\""))
    }

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

    @Test
    fun duplicateActiveShipmentsAreRejectedButCompletedHistoryIsAllowed() {
        val active = ShipmentRecord(
            id = "one", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.READY_FOR_PICKUP, createdAt = 1L, updatedAt = 2L
        )
        val duplicate = active.copy(id = "two", updatedAt = 3L)
        assertFalse(ShipmentRules.hasNoDuplicateActiveShipments(listOf(active, duplicate)))

        val completed = duplicate.copy(status = ShipmentStatus.HANDED_TO_CUSTOMER)
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(active, completed)))
    }

    @Test
    fun activeDuplicateDetectionIsCaseInsensitiveAndScopedToWorkflow() {
        val first = ShipmentRecord(
            id = "first", trackingNumber = "abC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.RECEIVED_BY_SHOP, createdAt = 1L, updatedAt = 1L
        )
        val sameNumberDifferentCase = first.copy(id = "second", trackingNumber = "ABC1234567")
        assertFalse(ShipmentRules.hasNoDuplicateActiveShipments(listOf(first, sameNumberDifferentCase)))

        val otherFlow = sameNumberDifferentCase.copy(
            id = "third",
            flow = ShipmentFlow.CUSTOMER_RETURN,
            status = ShipmentStatus.RETURN_ACCEPTED
        )
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(first, otherFlow)))
    }

    @Test
    fun completedShipmentsDoNotBlockFutureActiveRecords() {
        val completed = ShipmentRecord(
            id = "old", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.HANDED_TO_CUSTOMER, createdAt = 1L, updatedAt = 2L
        )
        val newActive = completed.copy(
            id = "new",
            status = ShipmentStatus.RECEIVED_BY_SHOP,
            createdAt = 3L,
            updatedAt = 3L
        )
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(completed, newActive)))
    }

    @Test
    fun sameTrackingNumberCanExistForDifferentWorkflowTypes() {
        val incoming = ShipmentRecord(
            id = "in", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.READY_FOR_PICKUP, createdAt = 1L, updatedAt = 2L
        )
        val returned = incoming.copy(
            id = "return", flow = ShipmentFlow.CUSTOMER_RETURN,
            status = ShipmentStatus.RETURN_ACCEPTED
        )
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(incoming, returned)))
    }
}
