package de.trakuna.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.net.Uri
import androidx.compose.ui.text.input.KeyboardCapitalization
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val Forest = Color(0xFF174B3A)
private const val CACHE_PREFS = "trakuna_handoff"
private const val CACHE_KEY = "tracking_number"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var number by remember { mutableStateOf("") }
            var showScanner by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Sendungsnummer scannen oder manuell eingeben.") }
            var showPartnerPicker by remember { mutableStateOf(false) }
            var showAllApps by remember { mutableStateOf(false) }
            var installedApps by remember { mutableStateOf<List<PartnerCandidate>>(emptyList()) }
            val context = LocalContext.current

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text("TRAKUNA", style = MaterialTheme.typography.headlineLarge, color = Forest)
                        Text("SCAN-ZENTRALE", style = MaterialTheme.typography.labelLarge)
                        Button(
                            onClick = { showScanner = !showScanner },
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        ) {
                            Text(if (showScanner) "Scanner schließen" else "Barcode / QR-Code scannen")
                        }
                        if (showScanner) {
                            ScannerPanel(
                                onDetected = { scanned ->
                                    number = scanned
                                    showScanner = false
                                    val result = handoffToPartnerApp(context, scanned, identifyCarrier(scanned))
                                    status = result.message
                                },
                                onClose = { showScanner = false }
                            )
                        }
                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sendungsnummer manuell eingeben") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            )
                        )
                        Button(
                            onClick = {
                                val result = handoffToPartnerApp(context, number, identifyCarrier(number))
                                status = result.message
                            },
                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("An Partner-App übergeben")
                        }
                        OutlinedButton(
                            onClick = {
                                installedApps = findLaunchableApps(context)
                                showAllApps = false
                                showPartnerPicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Betriebssoftware auf diesem Gerät finden") }
                        Text(
                            context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
                                .getString("partner_label", null)?.let { "Ausgewählte Betriebssoftware: $it" }
                                ?: "Noch keine Betriebssoftware ausgewählt",
                            style = MaterialTheme.typography.bodySmall,
                            color = Forest
                        )
                        Text(status, style = MaterialTheme.typography.bodyMedium, color = Forest)
                        if (showPartnerPicker) {
                            val candidates = if (showAllApps) installedApps else installedApps.filter { it.looksLikeCarrierApp }
                            AlertDialog(
                                onDismissRequest = { showPartnerPicker = false },
                                title = { Text("Betriebssoftware auswählen") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(if (candidates.isEmpty()) "Keine eindeutig erkennbare Paketdienst-App gefunden. Du kannst alle installierten Apps anzeigen." else "Wähle die Paketshop-/Betriebssoftware aus.")
                                        candidates.take(30).forEach { app ->
                                            TextButton(onClick = {
                                                context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE).edit()
                                                    .putString("partner_package", app.packageName)
                                                    .putString("partner_label", app.label)
                                                    .apply()
                                                status = "Betriebssoftware ${app.label} gespeichert. Die Sendungsnummer wird beim nächsten Übergabeversuch mitgegeben."
                                                showPartnerPicker = false
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                Text("${app.label}  ·  ${app.packageName}")
                                            }
                                        }
                                        if (!showAllApps) TextButton(onClick = { showAllApps = true }) { Text("Alle installierten Apps anzeigen") }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showPartnerPicker = false }) { Text("Schließen") } }
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Die letzte Sendungsnummer bleibt lokal gespeichert.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

data class HandoffResult(val message: String)

private data class PartnerCandidate(
    val label: String,
    val packageName: String,
    val looksLikeCarrierApp: Boolean
)

private fun findLaunchableApps(context: Context): List<PartnerCandidate> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0)
        .mapNotNull { info ->
            val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
            if (packageName == context.packageName) return@mapNotNull null
            val label = info.loadLabel(context.packageManager)?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapNotNull null
            val searchable = "$label $packageName".lowercase()
            val keywords = listOf("dhl", "dpd", "gls", "ups", "post", "paket", "parcel", "shipping", "versand", "zebra", "mde", "courier", "express")
            PartnerCandidate(label, packageName, keywords.any { searchable.contains(it) })
        }
        .distinctBy { it.packageName }
        .sortedWith(compareByDescending<PartnerCandidate> { it.looksLikeCarrierApp }.thenBy { it.label.lowercase() })
}


private fun handoffToPartnerApp(context: Context, rawNumber: String, carrier: String?): HandoffResult {
    val number = normalizeTrackingNumber(rawNumber)
    if (!isPlausibleTrackingNumber(number)) {
        return HandoffResult("Bitte eine gültige Sendungsnummer eingeben oder erneut scannen.")
    }

    // TRAKUNA cache: survives the app switch and remains available when the user returns.
    context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(CACHE_KEY, number)
        .putString("carrier", carrier.orEmpty())
        .putLong("saved_at", System.currentTimeMillis())
        .apply()

    // Clipboard is a compatibility fallback for partner apps that do not expose an intent API.
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("TRAKUNA Sendungsnummer", number))

    val preferences = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)\n    val packageName = PartnerAppConfig.packageFor(carrier) ?: preferences.getString("partner_package", null)
    if (packageName.isNullOrBlank()) {
        return HandoffResult(
            "Nummer $number ist im TRAKUNA-Cache gesichert und kopiert. " +
                "Wähle zuerst die Betriebssoftware über die Schaltfläche oben aus."
        )
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: return HandoffResult(
            "Nummer ist gesichert. Die konfigurierte Partner-App ($packageName) ist nicht installiert " +
                "oder Android kann sie nicht starten."
        )

    // Contract for an adapted partner app: read EXTRA_TRACKING_NUMBER from the launch intent.
    launchIntent.putExtra(EXTRA_TRACKING_NUMBER, number)
    launchIntent.putExtra(EXTRA_CARRIER, carrier.orEmpty())
    launchIntent.putExtra(Intent.EXTRA_TEXT, number)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    return try {
        context.startActivity(launchIntent)
        HandoffResult("Sendungsnummer gesichert. Partner-App wird geöffnet.")
    } catch (_: Exception) {
        HandoffResult("Nummer ist gesichert und kopiert, aber die Partner-App ließ sich nicht öffnen.")
    }
}

const val EXTRA_TRACKING_NUMBER = "de.trakuna.app.extra.TRACKING_NUMBER"
const val EXTRA_CARRIER = "de.trakuna.app.extra.CARRIER"

/** Public accessor for a future in-process adapter or diagnostics screen. */
fun getCachedTrackingNumber(context: Context): String =
    context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE).getString(CACHE_KEY, "").orEmpty()

private fun normalizeTrackingNumber(raw: String): String =
    raw.replace(Regex("[\\u0000-\\u001F]"), "").trim().replace(Regex("\\s+"), "")

private fun isPlausibleTrackingNumber(value: String): Boolean =
    value.length in 6..40 && value.any { it.isDigit() } && value.all { it.isLetterOrDigit() || it == '-' }

private fun extractTrackingNumber(raw: String): String? {
    val cleaned = normalizeTrackingNumber(raw).removePrefix("]C1").removePrefix("]d2")
    if (cleaned.startsWith("http://", true) || cleaned.startsWith("https://", true)) {
        val uri = runCatching { Uri.parse(cleaned) }.getOrNull()
        listOf("trackingNumber", "tracking", "shipment", "shipmentId", "parcel", "id", "piececode", "snr").forEach { key ->
            uri?.getQueryParameter(key)?.let { value ->
                normalizeTrackingNumber(value).takeIf { isPlausibleTrackingNumber(it) }?.let { return it }
            }
        }
        uri?.lastPathSegment?.let { segment ->
            normalizeTrackingNumber(segment).takeIf { isPlausibleTrackingNumber(it) }?.let { return it }
        }
        return null
    }
    return cleaned.takeIf { isPlausibleTrackingNumber(it) }
}

private fun identifyCarrier(raw: String): String? {
    val value = normalizeTrackingNumber(raw).uppercase()
    return when {
        Regex("^1Z[A-Z0-9]{16}$").matches(value) -> "UPS"
        Regex("^[A-Z]{2}[0-9]{9}AT$").matches(value) -> "Österreichische Post"
        value.startsWith("JVGL") || value.startsWith("JJD") -> "DHL Paket"
        value.startsWith("JD") && value.length in 12..30 -> "DHL Paket"
        value.startsWith("0034") && value.length >= 12 -> "DPD"
        else -> null
    }
}

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun ScannerPanel(onDetected: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Barcode- und QR-Scanner", style = MaterialTheme.typography.titleMedium)
            if (hasPermission) CameraPreview(onDetected)
            else {
                Text("Für den Scanner wird Kamerazugriff benötigt.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Kamerazugriff erlauben")
                }
            }
            TextButton(onClick = onClose) { Text("Schließen") }
        }
    }
}

@Composable
@OptIn(ExperimentalGetImage::class)
private fun CameraPreview(onDetected: (String) -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    if (activity == null) {
        Text("Kamera kann hier nicht gestartet werden.")
        return
    }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(280.dp))

    DisposableEffect(previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR, Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_DATA_MATRIX, Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        ).build()
        val scanner: BarcodeScanner = BarcodeScanning.getClient(options)
        val delivered = AtomicBoolean(false)
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val cameraProvider = future.get()
                provider = cameraProvider
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { proxy ->
                    val mediaImage = proxy.image
                    if (mediaImage == null || delivered.get()) {
                        proxy.close()
                    } else {
                        try {
                            scanner.process(InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees))
                                .addOnSuccessListener(mainExecutor) { barcodes ->
                                    val values = barcodes.mapNotNull { barcode ->
                                        (barcode.rawValue ?: barcode.displayValue)?.let { extractTrackingNumber(it) }
                                    }.distinct().sortedByDescending { it.length }
                                    values.firstOrNull()?.let { value ->
                                        if (delivered.compareAndSet(false, true)) onDetected(value)
                                    }
                                }
                                .addOnCompleteListener { proxy.close() }
                        } catch (_: Exception) {
                            proxy.close()
                        }
                    }
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(activity as LifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (_: Exception) {
                // Manual entry remains available if the camera cannot initialize.
            }
        }, mainExecutor)
        onDispose {
            runCatching { provider?.unbindAll() }
            scanner.close()
            executor.shutdown()
        }
    }
}
