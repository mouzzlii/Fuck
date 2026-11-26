package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScanCandidateConfirmationTest {
    @Test
    fun confirmsOnlyAfterRequiredConsecutiveFrames() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 3)
        assertNull(confirmation.observe(listOf("TRACK123456")))
        assertNull(confirmation.observe(listOf("TRACK123456")))
        assertEquals("TRACK123456", confirmation.observe(listOf("TRACK123456")))
    }

    @Test
    fun emptyFrameResetsConsecutiveConfirmation() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 3)
        confirmation.observe(listOf("TRACK123456"))
        confirmation.observe(listOf("TRACK123456"))
        assertNull(confirmation.observe(emptyList()))
        assertNull(confirmation.observe(listOf("TRACK123456")))
        assertNull(confirmation.observe(listOf("TRACK123456")))
        assertEquals("TRACK123456", confirmation.observe(listOf("TRACK123456")))
    }

    @Test
    fun ambiguousFrameResetsConfirmation() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 2)
        confirmation.observe(listOf("TRACK123456"))
        assertNull(confirmation.observe(listOf("TRACK123456", "TRACK654321")))
        assertNull(confirmation.observe(listOf("TRACK123456")))
        assertEquals("TRACK123456", confirmation.observe(listOf("TRACK123456")))
    }

    @Test
    fun changingCandidateStartsANewConfirmationSequence() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 2)
        confirmation.observe(listOf("TRACK123456"))
        assertNull(confirmation.observe(listOf("TRACK654321")))
        assertEquals("TRACK654321", confirmation.observe(listOf("TRACK654321")))
    }

    @Test
    fun duplicateObservationsOfSameCandidateInOneFrameCountOnce() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 2)
        assertNull(confirmation.observe(listOf("TRACK123456", "TRACK123456")))
        assertEquals("TRACK123456", confirmation.observe(listOf("TRACK123456")))
    }

    @Test
    fun ignoresBlankCandidatesAndRejectsInvalidConfirmationThreshold() {
        val confirmation = ScanCandidateConfirmation(requiredFrames = 1)
        assertNull(confirmation.observe(listOf("", "  ")))
        assertEquals("TRACK123456", confirmation.observe(listOf("TRACK123456")))
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            ScanCandidateConfirmation(requiredFrames = 0)
        }
    }
}
