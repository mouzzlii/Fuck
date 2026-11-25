package de.trakuna.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataWedgeSymbologyTest {
    @Test
    fun acceptsOnlySupportedQrAndLinearBarcodeTypes() {
        listOf(
            "LABEL-TYPE-QRCODE",
            "LABEL-TYPE-GS1-QRCODE",
            "LABEL-TYPE-CODE128",
            "LABEL-TYPE-EAN128",
            "LABEL-TYPE-CODE39",
            "LABEL-TYPE-CODE93",
            "LABEL-TYPE-CODABAR",
            "LABEL-TYPE-EAN13",
            "LABEL-TYPE-EAN8",
            "LABEL-TYPE-I2OF5",
            "LABEL-TYPE-UPCA",
            "LABEL-TYPE-UPCE0",
            "LABEL-TYPE-UPCE1"
        ).forEach { assertTrue("$it should be accepted", isAllowedDataWedgeLabelType(it)) }
    }

    @Test
    fun rejectsUnsupportedUnknownAndMissingTypes() {
        listOf(
            "LABEL-TYPE-DATAMATRIX",
            "LABEL-TYPE-GS1-DATAMATRIX",
            "LABEL-TYPE-PDF417",
            "LABEL-TYPE-AZTEC",
            "LABEL-TYPE-MICROQR",
            "LABEL-TYPE-HANXIN",
            "LABEL-TYPE-MSI",
            "LABEL-TYPE-OCR",
            "LABEL-TYPE-UNDEFINED",
            "unexpected"
        ).forEach { assertFalse("$it should be rejected", isAllowedDataWedgeLabelType(it)) }
        assertFalse(isAllowedDataWedgeLabelType(null))
        assertFalse(isAllowedDataWedgeLabelType("  "))
    }

    @Test
    fun normalizesCaseAndSurroundingWhitespace() {
        assertTrue(isAllowedDataWedgeLabelType(" label-type-qrcode "))
    }

    @Test
    fun android14AcceptsOnlyTheVerifiedDataWedgeSender() {
        assertTrue(isDataWedgeSenderAccepted(34, "com.symbol.datawedge"))
        assertFalse(isDataWedgeSenderAccepted(34, null))
        assertFalse(isDataWedgeSenderAccepted(34, "com.attacker.fake"))
    }

    @Test
    fun rejectsAllDataWedgeBroadcastsOnAndroidVersionsWithoutSenderIdentity() {
        assertFalse(isDataWedgeSenderAccepted(33, null))
        assertFalse(isDataWedgeSenderAccepted(26, null))
        assertFalse(isDataWedgeSenderAccepted(33, "com.symbol.datawedge"))
    }

    @Test
    fun explainsDisabledDataWedgeOnAndroidVersionsBelowApi34() {
        val message = dataWedgeRejectionMessage(33)
        org.junit.Assert.assertTrue(message.contains("deaktiviert"))
        org.junit.Assert.assertTrue(message.contains("Kamera oder manuelle Eingabe"))
    }

    @Test
    fun explainsUnverifiedSenderOnAndroid14AndNewer() {
        val message = dataWedgeRejectionMessage(34)
        org.junit.Assert.assertTrue(message.contains("nicht als DataWedge verifiziert"))
    }
    @Test
    fun identifiesEan13AsProductBarcodeWithoutRejectingOtherSymbologies() {
        assertTrue(isDataWedgeEan13("LABEL-TYPE-EAN13"))
        assertTrue(isDataWedgeEan13(" label-type-ean13 "))
        assertFalse(isDataWedgeEan13("LABEL-TYPE-EAN8"))
        assertFalse(isDataWedgeEan13("LABEL-TYPE-CODE128"))
        assertFalse(isDataWedgeEan13(null))
    }

    @Test
    fun rejectsEan13PayloadOnlyWhenSymbologyConfirmsItIsEan13() {
        val productCode = "8721335813708"
        assertFalse(isValidTrackingNumber(extractTrackingNumber(productCode, rejectEan13 = isDataWedgeEan13("LABEL-TYPE-EAN13")) ?: ""))
        assertTrue(isValidTrackingNumber(extractTrackingNumber(productCode, rejectEan13 = isDataWedgeEan13("LABEL-TYPE-CODE128")) ?: ""))
    }

}
