package de.trakuna.app

import android.content.ClipDescription
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SensitiveDiagnosticClipboardInstrumentedTest {
    @Test
    fun diagnosticClipboardMarksRawShipmentDataAsSensitiveOnAndroid13AndLater() {
        val report = "TRAKUNA Scan-Diagnose\\nRohinhalt: TEST-SHIPMENT-123"
        val clip = createSensitiveDiagnosticClip(report)

        assertEquals(report, clip.getItemAt(0).text.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = clip.description.extras
            assertNotNull("Android 13+ muss Clipboard-Metadaten bereitstellen.", extras)
            assertTrue(
                "Diagnoseinhalte mit Sendungsdaten müssen als sensibel markiert sein.",
                extras!!.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false)
            )
        } else {
            val extras = clip.description.extras
            assertFalse(
                "Vor Android 13 darf kein nicht unterstütztes Sensitivitätsflag behauptet werden.",
                extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true
            )
        }
    }
}
