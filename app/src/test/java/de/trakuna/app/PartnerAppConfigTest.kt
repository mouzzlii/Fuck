package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PartnerAppConfigTest {
    @Test
    fun explicitOperatorChoiceTakesPrecedenceOverCarrierDefault() {
        assertEquals("operator.parcelshop.app", PartnerAppConfig.resolvePackage("DHL Paket", "operator.parcelshop.app"))
    }

    @Test
    fun blankOperatorChoiceFallsBackToCarrierDefault() {
        assertEquals(PartnerAppConfig.packageFor("DHL Paket"), PartnerAppConfig.resolvePackage("DHL Paket", "   "))
    }

    @Test
    fun missingOperatorChoiceAndCarrierUseConfiguredDefault() {
        assertEquals(PartnerAppConfig.packageFor(null), PartnerAppConfig.resolvePackage(null, null))
    }

    @Test
    fun carrierSpecificAppNeverFallsBackToAnotherCarriersOrLegacyApp() {
        assertEquals(
            "dpd.parcelshop.app",
            PartnerAppConfig.resolveSelectedPackage("DPD", " dpd.parcelshop.app ", true, "legacy.shared.app")
        )
        assertNull(
            PartnerAppConfig.resolveSelectedPackage("GLS", null, true, "legacy.shared.app")
        )
    }

    @Test
    fun legacySharedAppRemainsAvailableUntilFirstPerCarrierAssignment() {
        assertEquals(
            "legacy.shared.app",
            PartnerAppConfig.resolveSelectedPackage("UPS", null, false, " legacy.shared.app ")
        )
        assertNull(PartnerAppConfig.resolveSelectedPackage("UPS", null, false, "  "))
    }

    @Test
    fun priorityCarriersHaveDistinctStablePreferenceSuffixes() {
        assertEquals(listOf("DPD", "GLS", "Hermes", "UPS"), PartnerAppConfig.priorityCarriers)
        assertEquals("DPD", PartnerAppConfig.preferenceSuffixForCarrier(" dpd "))
        assertEquals("GLS", PartnerAppConfig.preferenceSuffixForCarrier("gls"))
        assertEquals("Hermes", PartnerAppConfig.preferenceSuffixForCarrier(" HERMES "))
        assertEquals("UPS", PartnerAppConfig.preferenceSuffixForCarrier("ups"))
        assertNull(PartnerAppConfig.preferenceSuffixForCarrier("DHL Paket"))
    }
}
