package de.trakuna.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarrierPortalDomainsTest {
    @Test
    fun recognizesOfficialCarrierHostsWithSubdomainBoundaries() {
        assertEquals("DPD", CarrierPortalDomains.identifyFromUrl("https://www.dpd.com/track"))
        assertEquals("GLS", CarrierPortalDomains.identifyFromUrl("https://gls-one.at/track"))
        assertEquals("Hermes", CarrierPortalDomains.identifyFromUrl("https://tracking.hermesworld.com/track"))
        assertEquals("UPS", CarrierPortalDomains.identifyFromUrl("https://www.ups.com/track"))
        assertEquals("Österreichische Post", CarrierPortalDomains.identifyFromUrl("https://www.post.at/track"))
        assertEquals("DHL Paket", CarrierPortalDomains.identifyFromUrl("https://www.dhl.de/track"))
        assertEquals("DHL Express", CarrierPortalDomains.identifyFromUrl("https://mydhl.express.dhl/track"))
        assertEquals("FedEx", CarrierPortalDomains.identifyFromUrl("https://www.fedex.com/track"))
        assertEquals("LogoiX", CarrierPortalDomains.identifyFromUrl("https://www.logoix.com/track"))
        assertNull(CarrierPortalDomains.identifyFromUrl("https://dpd.com.attacker.example/track"))
        assertNull(CarrierPortalDomains.identifyFromUrl("javascript://dpd.com/track"))
        assertNull(CarrierPortalDomains.identifyFromUrl("https://user@www.dpd.com/track"))
    }

    @Test
    fun portalUrlsMustUseHttpsAndTheMatchingOfficialCarrierDomain() {
        assertEquals(
            "https://business.dpd.com/parcel",
            CarrierPortalDomains.validatePortalUrl("DPD", "https://business.dpd.com/parcel")
        )
        assertNull(CarrierPortalDomains.validatePortalUrl("DPD", "http://www.dpd.com/parcel"))
        assertNull(CarrierPortalDomains.validatePortalUrl("DPD", "https://www.ups.com/parcel"))
        assertNull(CarrierPortalDomains.validatePortalUrl("DPD", "https://dpd.com.attacker.example/parcel"))
        assertNull(CarrierPortalDomains.validatePortalUrl("DPD", "https://user@www.dpd.com/parcel"))
        assertNull(CarrierPortalDomains.validatePortalUrl("Unknown", "https://www.dpd.com/parcel"))
    }
}
