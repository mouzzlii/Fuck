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
    fun updateTimestampNeverMovesBehindPreviousRecordWhenDeviceClockGoesBackwards() {
        assertEquals(2_000L, ShipmentRules.nextUpdateTimestamp(2_000L, 1_000L))
        assertEquals(3_000L, ShipmentRules.nextUpdateTimestamp(2_000L, 3_000L))
    }

    @Test
    fun timestampExportUsesStableUtcIso8601Format() {
        assertEquals("1970-01-01T00:00:00Z", ShipmentRules.formatTimestampForExport(0L))
    }

    @Test
    fun timestampDisplayUsesGermanDateAndTimeOrder() {
        assertEquals("01.01.1970 00:00:00", ShipmentRules.formatTimestamp(0L, java.time.ZoneOffset.UTC))
    }

    @Test
    fun csvExportEscapesSpreadsheetFormulasAndQuotes() {
        assertEquals("\"'=1+1\"", ShipmentRules.csvSafeCell("=1+1"))
        assertEquals("\"'  @SUM(A1:A2)\"", ShipmentRules.csvSafeCell("  @SUM(A1:A2)"))
        assertEquals("\"'\\t=1+1\"", ShipmentRules.csvSafeCell("\t=1+1"))
        assertEquals("\"'\\uFEFF=1+1\"", ShipmentRules.csvSafeCell("\uFEFF=1+1"))
        assertEquals("\"'\\u200B+1+1\"", ShipmentRules.csvSafeCell("\u200B+1+1"))
        assertEquals("\"normal text\"", ShipmentRules.csvSafeCell("normal text"))
        assertEquals("\"say \"\"hello\"\"\"", ShipmentRules.csvSafeCell("say \"hello\""))
    }

    @Test
    fun carrierLabelsAreTrimmedAndBoundedToStorageLimit() {
        assertEquals("Österreichische Post", ShipmentRules.normalizeCarrierLabel("  Österreichische Post  "))
        assertEquals(80, ShipmentRules.normalizeCarrierLabel("X".repeat(120)).length)
        assertEquals("", ShipmentRules.normalizeCarrierLabel("   "))
    }

    @Test
    fun normalizesAndValidatesTrackingNumbers() {
        assertEquals("1Z999AA10123456784", ShipmentRules.normalizeTrackingNumber(" 1Z 999AA10123456784 "))
        assertTrue(ShipmentRules.isValidTrackingNumber("1Z999AA10123456784"))
        assertFalse(ShipmentRules.isValidTrackingNumber("ABC"))
        assertFalse(ShipmentRules.isValidTrackingNumber("123456!"))
        assertFalse(ShipmentRules.isValidTrackingNumber("ÄBC123456"))
        assertFalse(ShipmentRules.isValidTrackingNumber("ＡBC123456"))
        assertTrue(ShipmentRules.isValidTrackingNumber("ABC123456"))
        assertFalse(ShipmentRules.isValidTrackingNumber("12345678901234567890123456789012345678901"))
    }

    @Test
    fun trackingNumberNormalizationDoesNotSilentlyStripControlCharacters() {
        val raw = "ABC\u0000123456"
        val normalized = ShipmentRules.normalizeTrackingNumber(raw)
        assertFalse(ShipmentRules.isValidTrackingNumber(normalized))
        assertEquals("ABC123456", ShipmentRules.normalizeTrackingNumber(" ABC 123456 "))
    }

    @Test
    fun manualCarrierChoiceOverridesHintAndAmbiguousNumbersStayUnassigned() {
        assertEquals("DHL", ShipmentRules.resolveCarrierLabel(" DHL ", "12345678901234"))
        assertEquals("", ShipmentRules.resolveCarrierLabel("", "12345678901234"))
        assertEquals("GLS", ShipmentRules.resolveCarrierLabel("GLS", "1Z999AA10123456784"))
        assertEquals("", ShipmentRules.resolveCarrierLabel(" ", "ABC1234567"))
    }

    @Test
    fun duplicateOutcomeRetainsExistingRecordAndIsNotReportedAsNewSave() {
        val existing = ShipmentRecord(
            id = "existing", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.READY_FOR_PICKUP, createdAt = 1L, updatedAt = 2L
        )
        val outcome: ShipmentUpdate = ShipmentUpdate.AlreadyExists(existing)
        assertTrue(outcome is ShipmentUpdate.AlreadyExists)
        assertEquals(existing, (outcome as ShipmentUpdate.AlreadyExists).record)
    }

    @Test
    fun carrierHintsRecognizeOnlyDistinctiveFormatsAndLeaveNumericValuesUnknown() {
        assertEquals("UPS", ShipmentRules.identifyCarrier("1z999aa10123456784"))
        assertEquals("Österreichische Post", ShipmentRules.identifyCarrier("AB123456789AT"))
        assertEquals(null, ShipmentRules.identifyCarrier("1234567890123"))
        assertEquals(null, ShipmentRules.identifyCarrier("12345678901234"))
        assertEquals(null, ShipmentRules.identifyCarrier("12345678901"))
        assertEquals("DHL Paket", ShipmentRules.identifyCarrier("JVGL1234567890"))
        assertEquals(null, ShipmentRules.identifyCarrier("ABC1234567"))
        assertEquals(null, ShipmentRules.identifyCarrier("123456789012345"))
    }

    @Test
    fun incomingShipmentFollowsPickupLifecycle() {
        assertEquals(ShipmentStatus.RECEIVED_BY_SHOP, ShipmentRules.initialStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER))
        assertEquals(ShipmentStatus.READY_FOR_PICKUP, ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.RECEIVED_BY_SHOP))
        assertEquals(ShipmentStatus.HANDED_TO_CUSTOMER, ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.READY_FOR_PICKUP))
        assertEquals(null, ShipmentRules.nextStatus(ShipmentFlow.INCOMING_FOR_CUSTOMER, ShipmentStatus.HANDED_TO_CUSTOMER))
    }

    @Test
    fun outgoingAndReturnFlowsFinishAtCarrierHandover() {
        assertEquals(ShipmentStatus.HANDED_TO_CARRIER, ShipmentRules.nextStatus(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.ACCEPTED_FROM_CUSTOMER))
        assertEquals(ShipmentStatus.HANDED_TO_CARRIER, ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.RETURN_ACCEPTED))
        assertEquals(null, ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.HANDED_TO_CARRIER))
        assertEquals(null, ShipmentRules.nextStatus(ShipmentFlow.OUTGOING_FROM_CUSTOMER, ShipmentStatus.RETURN_ACCEPTED))
        assertEquals(null, ShipmentRules.nextStatus(ShipmentFlow.CUSTOMER_RETURN, ShipmentStatus.ACCEPTED_FROM_CUSTOMER))
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
        val otherFlow = sameNumberDifferentCase.copy(id = "third", flow = ShipmentFlow.CUSTOMER_RETURN, status = ShipmentStatus.RETURN_ACCEPTED)
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(first, otherFlow)))
    }

    @Test
    fun completedShipmentsDoNotBlockFutureActiveRecords() {
        val completed = ShipmentRecord(
            id = "old", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.HANDED_TO_CUSTOMER, createdAt = 1L, updatedAt = 2L
        )
        val newActive = completed.copy(id = "new", status = ShipmentStatus.RECEIVED_BY_SHOP, createdAt = 3L, updatedAt = 3L)
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(completed, newActive)))
    }

    @Test
    fun sameTrackingNumberCanExistForDifferentWorkflowTypes() {
        val incoming = ShipmentRecord(
            id = "in", trackingNumber = "ABC1234567", carrier = "DHL",
            flow = ShipmentFlow.INCOMING_FOR_CUSTOMER,
            status = ShipmentStatus.READY_FOR_PICKUP, createdAt = 1L, updatedAt = 2L
        )
        val returned = incoming.copy(id = "return", flow = ShipmentFlow.CUSTOMER_RETURN, status = ShipmentStatus.RETURN_ACCEPTED)
        assertTrue(ShipmentRules.hasNoDuplicateActiveShipments(listOf(incoming, returned)))
    }

    @Test
    fun scanParserHandlesAIMPrefixesAndPlainTrackingNumbers() {
        assertEquals("1Z999AA10123456784", ShipmentRules.extractTrackingNumber("]C11Z999AA10123456784"))
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber(" AB123456789AT "))
        assertEquals(null, ShipmentRules.extractTrackingNumber("Parcel 12345678"))
        assertEquals(null, ShipmentRules.extractTrackingNumber("Invoice 123456789"))
        assertEquals("1Z999AA10123456784", ShipmentRules.extractTrackingNumber("1Z 999AA10123456784"))
    }

    @Test
    fun scanParserReadsSupportedQrQueryParametersCaseInsensitively() {
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber("https://example.test/track?Tracking_Number=AB123456789AT"))
        assertEquals("1Z999AA10123456784", ShipmentRules.extractTrackingNumber("https://example.test/track?id=1Z999AA10123456784"))
    }

    @Test
    fun scanParserUsesRecognizableQrPathAndRejectsUntrustedOrOversizedInput() {
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber("https://example.test/parcel/AB123456789AT"))
        assertEquals(null, ShipmentRules.extractTrackingNumber("https://example.test/parcel/not-a-number"))
        assertEquals(null, ShipmentRules.extractTrackingNumber("x".repeat(4097)))
        assertEquals(null, ShipmentRules.extractTrackingNumber("https://example.test/track?id=%ZZ"))
    }

    @Test
    fun scanParserContinuesAfterInvalidRecognizedQueryParameter() {
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber("https://example.test/track?tracking=invalid&sendungsnummer=AB123456789AT"))
    }

    @Test
    fun scanParserDecodesPercentEncodedTrackingNumbersAndAIMQrPrefix() {
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber("https://example.test/track?tracking=AB123456789%41T"))
        assertEquals("AB123456789AT", ShipmentRules.extractTrackingNumber("]Q3AB123456789AT"))
    }

    @Test
    fun scanParserDoesNotAcceptUnicodeLookalikesOrNonHttpUrlSchemes() {
        assertEquals(null, ShipmentRules.extractTrackingNumber("https://example.test/track?id=ＡＢ123456789AT"))
        assertEquals(null, ShipmentRules.extractTrackingNumber("javascript://example.test/track?id=AB123456789AT"))
    }
}
