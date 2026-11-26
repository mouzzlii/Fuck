package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShadowTestModeTest {
    @Test
    fun pilotBuildIsLockedToShadowMode() {
        assertTrue(TrakunaTestMode.SHADOW_MODE_ENABLED)
    }

    @Test
    fun knownCarrierIsRecordedWithoutShipmentInformation() {
        assertEquals("DPD", TrakunaTestMode.auditCarrier("DPD"))
        val message = TrakunaTestMode.blockedHandoffMessage("DPD")
        assertTrue(message.contains("TESTMODUS"))
        assertTrue(message.contains("keine Buchung"))
        assertTrue(message.contains("NICHT geöffnet"))
        assertFalse(message.contains("TRACK123"))
    }

    @Test
    fun unknownCarrierIsRedactedInAuditAndMessage() {
        assertEquals("OTHER", TrakunaTestMode.auditCarrier("private customer note"))
        assertFalse(TrakunaTestMode.blockedHandoffMessage("private customer note").contains("private customer note"))
    }
}
