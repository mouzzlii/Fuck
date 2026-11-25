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
}
