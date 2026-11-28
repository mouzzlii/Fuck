package de.trakuna.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ShadowAuditLogInstrumentedTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val logFile: File
        get() = File(File(context.filesDir, "test-evidence"), "shadow-events.jsonl")

    @Before
    fun clearPreviousLog() {
        logFile.delete()
    }

    @After
    fun removeTestLog() {
        logFile.delete()
    }

    @Test
    fun blockedPartnerHandoffIsLoggedWithoutShipmentOrCustomerData() {
        assertTrue(ShadowAuditLog.recordPartnerHandoff(context, "DPD"))

        val report = ShadowAuditLog.readAll(context)
        assertTrue(report.contains("PARTNER_HANDOFF_BLOCKED"))
        assertTrue(report.contains("SHADOW"))
        assertTrue(report.contains("DPD"))
        assertTrue(report.contains("BLOCKED_BEFORE_EXTERNAL_APP"))
        assertFalse(report.contains("TEST123456"))
        assertFalse(report.contains("customer name"))
    }

    @Test
    fun unsupportedCarrierIsRedactedInSharedAuditLog() {
        assertTrue(ShadowAuditLog.recordPartnerHandoff(context, "private customer note"))

        val report = ShadowAuditLog.readAll(context)
        assertTrue(report.contains("OTHER"))
        assertFalse(report.contains("private customer note"))
    }
}
