package de.trakuna.app

import org.junit.Assert.assertEquals
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
}
