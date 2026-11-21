package de.trakuna.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val Forest = Color(0xFF174B3A)
private val carriers = listOf("Österreichische Post", "DHL Paket", "DHL Express", "Deutsche Post", "DPD", "GLS", "UPS", "Hermes", "FedEx", "Sonstiger")
private val workflows = listOf("Annahme", "Abholung", "Retoure", "Versand")

data class Parcel(val code: String, val carrier: String, val customer: String, val status: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("trakuna_data", MODE_PRIVATE)
        setContent {
            var parcels by remember { mutableStateOf(loadParcels(preferences.getString("parcels", "[]") ?: "[]")) }
            var selectedWorkflow by remember { mutableStateOf("Annahme") }
            var code by remember { mutableStateOf("") }
            var customer by remember { mutableStateOf("") }
            var search by remember { mutableStateOf("") }
            var carrier by remember { mutableStateOf(carriers.first()) }
            var carrierMenu by remember { mutableStateOf(false) }
            var showScanner by remember { mutableStateOf(false) }
            var message by remember { mutableStateOf("Lokaler Testbetrieb – Scanner und manuelle Erfassung verfügbar.") }

            fun saveParcels(updated: List<Parcel>) {
                parcels = updated
                val json = JSONArray()
                updated.forEach { parcel ->
                    json.put(JSONObject().put("code", parcel.code).put("carrier", parcel.carrier).put("customer", parcel.customer).put("status", parcel.status))
                }
                preferences.edit().putString("parcels", json.toString()).apply()
            }

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("TRAKUNA", style = MaterialTheme.typography.headlineLarge, color = Forest)
                        Text("ALLE PAKETDIENSTE. EIN SMARTPHONE.", style = MaterialTheme.typography.labelLarge)
                        Text(message, style = MaterialTheme.typography.bodySmall)

                        Text("Paketshop-Ablauf", style = MaterialTheme.typography.titleMedium)
                        workflows.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { workflow ->
                                    Button(
                                        onClick = { selectedWorkflow = workflow },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedWorkflow == workflow) Forest else Color.Gray)
                                    ) { Text(workflow) }
                                }
                            }
                        }

                        Button(onClick = { showScanner = !showScanner }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (showScanner) "Scanner schließen" else "Kamera-Barcode / QR scannen")
                        }
                        if (showScanner) {
                            ScannerPanel(
                                onDetected = { value ->
                                    code = value
                                    showScanner = false
                                    message = "Code erkannt: $value"
                                },
                                onClose = { showScanner = false }
                            )
                        }

                        OutlinedTextField(
                            value = code, onValueChange = { code = it }, modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sendungsnummer / Barcode (manuell möglich)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), singleLine = true
                        )
                        OutlinedTextField(value = customer, onValueChange = { customer = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Kundenname (optional)") }, singleLine = true)

                        Box {
                            OutlinedButton(onClick = { carrierMenu = true }, modifier = Modifier.fillMaxWidth()) { Text("Paketdienst: $carrier") }
                            DropdownMenu(expanded = carrierMenu, onDismissRequest = { carrierMenu = false }) {
                                carriers.forEach { item ->
                                    DropdownMenuItem(text = { Text(item) }, onClick = { carrier = item; carrierMenu = false })
                                }
                            }
                        }

                        Button(onClick = {
                            val number = code.trim()
                            if (number.isBlank()) {
                                message = "Bitte Sendungsnummer eingeben oder scannen."
                            } else {
                                val index = parcels.indexOfFirst { it.code == number }
                                when (selectedWorkflow) {
                                    "Annahme" -> {
                                        if (index >= 0) message = "Diese Sendungsnummer existiert bereits."
                                        else {
                                            saveParcels(parcels + Parcel(number, carrier, customer.trim(), "Eingelagert"))
                                            message = "Sendung lokal angenommen."
                                            code = ""; customer = ""
                                        }
                                    }
                                    "Retoure" -> {
                                        saveParcels(parcels + Parcel(number, carrier, customer.trim(), "Retoure erfasst"))
                                        message = "Retoure lokal erfasst."
                                        code = ""; customer = ""
                                    }
                                    "Abholung" -> {
                                        if (index < 0) message = "Sendung wurde lokal nicht gefunden."
                                        else {
                                            val updated = parcels.toMutableList()
                                            updated[index] = updated[index].copy(status = "Abgeholt")
                                            saveParcels(updated); message = "Abholung lokal gespeichert."; code = ""
                                        }
                                    }
                                    "Versand" -> {
                                        if (index < 0) message = "Sendung wurde lokal nicht gefunden."
                                        else {
                                            val updated = parcels.toMutableList()
                                            updated[index] = updated[index].copy(status = "Zur Übergabe vorgemerkt")
                                            saveParcels(updated); message = "Übergabe lokal vorgemerkt."; code = ""
                                        }
                                    }
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("$selectedWorkflow speichern") }

                        HorizontalDivider()
                        Text("Sendungen (${parcels.size})", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Sendungen durchsuchen") }, singleLine = true)
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(parcels.filter { it.code.contains(search, true) || it.customer.contains(search, true) }) { parcel ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(parcel.code, style = MaterialTheme.typography.titleMedium)
                                        Text(parcel.carrier)
                                        if (parcel.customer.isNotBlank()) Text(parcel.customer)
                                        Text(parcel.status, color = Forest)
                                    }
                                }
                            }
                        }
                        Text("Lokaler Testbetrieb – keine echte Carrier-Buchung oder Online-Synchronisierung.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    private fun loadParcels(json: String): List<Parcel> = try {
        val array = JSONArray(json)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Parcel(item.getString("code"), item.getString("carrier"), item.optString("customer"), item.getString("status"))
        }
    } catch (_: Exception) { emptyList() }
}

@Composable
private fun ScannerPanel(onDetected: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Barcode- und QR-Scanner", style = MaterialTheme.typography.titleMedium)
            if (hasPermission) {
                CameraPreview(onDetected = onDetected)
            } else {
                Text("Für das Scannen wird Kamerazugriff benötigt.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Kamerazugriff erlauben")
                }
            }
            TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Schließen") }
        }
    }
}

@Composable
@OptIn(ExperimentalGetImage::class)
private fun CameraPreview(onDetected: (String) -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    if (activity == null) {
        Text("Kamera kann in diesem Kontext nicht gestartet werden.")
        return
    }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(260.dp))

    DisposableEffect(previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val scannerOptions = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(scannerOptions)
        val delivered = AtomicBoolean(false)
        var cameraProvider: ProcessCameraProvider? = null

        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                cameraProvider = provider
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null || delivered.get()) {
                        imageProxy.close()
                    } else {
                        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        barcodeScanner.process(input)
                            .addOnSuccessListener { barcodes ->
                                val value = barcodes.firstNotNullOfOrNull { it.rawValue?.takeIf(String::isNotBlank) }
                                if (value != null && delivered.compareAndSet(false, true)) onDetected(value)
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                }
                provider.unbindAll()
                provider.bindToLifecycle(activity as LifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            } catch (_: Exception) {
                // Permission/device/camera errors leave manual entry available.
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try { cameraProvider?.unbindAll() } catch (_: Exception) { }
            barcodeScanner.close()
            executor.shutdown()
        }
    }
}
