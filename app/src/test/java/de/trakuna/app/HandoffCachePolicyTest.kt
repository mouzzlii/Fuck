package de.trakuna.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HandoffCachePolicyTest {
    @Test
    fun handoffRemainsValidWithinTwentyFourHours() {
        val now = 1_800_000_000_000L
        assertFalse(isHandoffExpired(now - HANDOFF_TTL_MS, now))
        assertFalse(isHandoffExpired(now - HANDOFF_TTL_MS + 1L, now))
    }

    @Test
    fun handoffExpiresAfterTwentyFourHours() {
        val now = 1_800_000_000_000L
        assertTrue(isHandoffExpired(now - HANDOFF_TTL_MS - 1L, now))
    }

    @Test
    fun invalidOrImplausiblyFutureTimestampIsExpired() {
        val now = 1_800_000_000_000L
        assertTrue(isHandoffExpired(-1L, now))
        assertTrue(isHandoffExpired(now + HANDOFF_CLOCK_SKEW_TOLERANCE_MS + 1L, now))
        assertFalse(isHandoffExpired(now + HANDOFF_CLOCK_SKEW_TOLERANCE_MS, now))
    }
}
