package de.trakuna.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CaptureRequest
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/** CameraX/ML Kit scanner UI and lifecycle, isolated from the activity's parcel workflow. */
private fun barcodeFormatLabel(format: Int): String = when (format) {
    Barcode.FORMAT_QR_CODE -> "QR-Code"
    Barcode.FORMAT_CODE_128 -> "Code 128"
    Barcode.FORMAT_CODE_39 -> "Code 39"
    Barcode.FORMAT_CODE_93 -> "Code 93"
    Barcode.FORMAT_CODABAR -> "Codabar"
    Barcode.FORMAT_EAN_13 -> "EAN-13"
    Barcode.FORMAT_EAN_8 -> "EAN-8"
    Barcode.FORMAT_ITF -> "ITF"
    Barcode.FORMAT_UPC_A -> "UPC-A"
    Barcode.FORMAT_UPC_E -> "UPC-E"
    Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
    Barcode.FORMAT_PDF417 -> "PDF417"
    Barcode.FORMAT_AZTEC -> "Aztec"
    else -> "Unbekannt ($format)"
}

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
internal fun ScannerPanel(onDetected: (ScanDiagnosticResult) -> Unit, onClose: () -> Unit) {
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

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class, androidx.camera.camera2.interop.ExperimentalCamera2Interop::class])
@Composable
private fun CameraPreview(onDetected: (ScanDiagnosticResult) -> Unit) {
    val context = LocalContext.current
    val activity = context.findActivity()
    if (activity == null) {
        Text("Kamera kann hier nicht gestartet werden.")
        return
    }
    var cameraError by remember { mutableStateOf<String?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxWidth().height(280.dp))
    cameraError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

    DisposableEffect(previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR, Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E
        ).build()
        val scanner: BarcodeScanner = BarcodeScanning.getClient(options)
        val delivered = AtomicBoolean(false)
        val disposed = AtomicBoolean(false)
        // Camera frames can reveal different barcodes on the same label at slightly
        // different times. Collect candidates across several frames before choosing.
        val observedObservations = ConcurrentHashMap.newKeySet<ScanObservation>()
        val framesAfterFirstCandidate = AtomicInteger(0)
        val requiredConfirmationFrames = 12
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed.get()) try {
                val cameraProvider = future.get()
                if (disposed.get()) return@addListener
                provider = cameraProvider
                val previewBuilder = Preview.Builder()
                Camera2Interop.Extender(previewBuilder).setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                val preview = previewBuilder.build().also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { proxy ->
                    val mediaImage = proxy.image
                    if (mediaImage == null || delivered.get() || disposed.get()) {
                        proxy.close()
                    } else {
                        try {
                            scanner.process(InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees))
                                .addOnSuccessListener(mainExecutor) { barcodes ->
                                    if (!disposed.get()) {
                                        val observations = barcodes.mapNotNull { barcode ->
                                            val raw = barcode.rawValue?.takeIf { it.isNotBlank() }
                                                ?: barcode.displayValue?.takeIf { it.isNotBlank() }
                                                ?: return@mapNotNull null
                                            if (raw.length > MAX_SCAN_INPUT_CHARS) return@mapNotNull null
                                            ScanObservation(
                                                source = "Kamera / ML Kit",
                                                format = barcodeFormatLabel(barcode.format),
                                                rawValue = raw,
                                                displayValue = barcode.displayValue,
                                                extractedValue = extractTrackingNumber(
                                                    raw,
                                                    rejectEan13 = barcode.format == Barcode.FORMAT_EAN_13
                                                )
                                            )
                                        }
                                        observedObservations.addAll(observations)
                                        if (observedObservations.isNotEmpty() &&
                                            framesAfterFirstCandidate.incrementAndGet() >= requiredConfirmationFrames &&
                                            !disposed.get() && delivered.compareAndSet(false, true)
                                        ) {
                                            // Preserve every distinct raw code for diagnosis. Select a
                                            // tracking number only when exactly one distinct candidate exists.
                                            val snapshot = observedObservations.toList().sortedWith(
                                                compareBy<ScanObservation> { it.format }.thenBy { it.rawValue }
                                            )
                                            val candidates = snapshot.mapNotNull { it.extractedValue }.distinct()
                                            onDetected(
                                                ScanDiagnosticResult(
                                                    observations = snapshot,
                                                    selectedValue = candidates.singleOrNull()
                                                )
                                            )
                                        }
                                    }
                                }
                                .addOnCompleteListener { proxy.close() }
                        } catch (_: Exception) {
                            proxy.close()
                        }
                    }
                }
                if (!disposed.get()) {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(activity as LifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    previewView.setOnClickListener { }
                    previewView.setOnTouchListener { view, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            val point = previewView.meteringPointFactory.createPoint(event.x, event.y)
                            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS).build()
                            runCatching { camera.cameraControl.startFocusAndMetering(action) }
                            view.performClick()
                            true
                        } else true
                    }
                    previewView.post {
                        if (!disposed.get() && previewView.width > 0 && previewView.height > 0) {
                            val centerPoint = previewView.meteringPointFactory.createPoint(previewView.width / 2f, previewView.height / 2f)
                            val initialFocus = FocusMeteringAction.Builder(centerPoint, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS).build()
                            runCatching { camera.cameraControl.startFocusAndMetering(initialFocus) }
                        }
                    }
                }
            } catch (error: Exception) {
                if (!disposed.get()) {
                    cameraError = "Kamera konnte nicht gestartet werden. Bitte Kameraberechtigung prüfen oder Sendungsnummer manuell eingeben."
                }
            }
        }, mainExecutor)
        onDispose {
            disposed.set(true)
            runCatching { provider?.unbindAll() }
            scanner.close()
            executor.shutdown()
        }
    }
}
