package de.trakuna.app

import android.Manifest
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.text.input.KeyboardCapitalization
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap

private val Forest = Color(0xFF174B3A)
private const val CACHE_PREFS = "trakuna_handoff"
private const val DATAWEDGE_ACTION = "de.trakuna.app.DATAWEDGE_SCAN"
private const val DATAWEDGE_DATA_EXTRA = "com.symbol.datawedge.data_string"
private const val MAX_SCAN_INPUT_CHARS = 4096

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var number by remember { mutableStateOf("") }
            var showScanner by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Sendungsnummer scannen oder manuell eingeben.") }
            var showShipmentLedger by remember { mutableStateOf(false) }
            var shipmentRevision by remember { mutableStateOf(0) }
            var carrierOverride by remember { mutableStateOf("") }
            var shipmentSearch by remember { mutableStateOf("") }
            var showArchivedShipments by remember { mutableStateOf(false) }
            var visibleRecordLimit by remember { mutableStateOf(100) }
            var pendingAdvance by remember { mutableStateOf<ShipmentRecord?>(null) }
            val context = LocalContext.current
            // Keep one store instance for the screen: its synchronization and initialization state
            // must be shared by every read/write operation in this UI.
            val shipmentStore = remember(context) { ShipmentStore(context) }
            DisposableEffect(context) {
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        // Ignore broadcasts while TRAKUNA is backgrounded or no longer active.
                        if (!this@MainActivity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
                        if (intent?.action != DATAWEDGE_ACTION) return
                        // Broadcasts are externally supplied; ignore a missing or wrongly typed
                        // extra instead of allowing a malformed parcel to crash the receiver.
                        val raw = runCatching { intent.getStringExtra(DATAWEDGE_DATA_EXTRA) }.getOrNull() ?: return
                        if (raw.length > MAX_SCAN_INPUT_CHARS) {
                            status = "Scaninhalt ist zu lang und wurde aus Sicherheitsgründen abgelehnt. Bitte Sendungsnummer manuell prüfen."
                            return
                        }
                        val scanned = extractTrackingNumber(raw)
                        if (scanned.isNullOrBlank()) {
                            status = "Zebra-Scan empfangen, aber keine unterstützte Sendungsnummer erkannt. Bitte manuell prüfen."
                            return
                        }
                        number = scanned
                        status = "Zebra-Scan empfangen. Sendungsnummer bitte prüfen und den nächsten Schritt auswählen."
                    }
                }
                val filter = IntentFilter(DATAWEDGE_ACTION)
                // DataWedge is a separate Android app, so the receiver must be exported.
                // ContextCompat supplies the required explicit flag on every supported API level.
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
                onDispose { runCatching { context.unregisterReceiver(receiver) } }
            }
            var pendingBackupJson by remember { mutableStateOf<String?>(null) }
            var confirmBackupExport by remember { mutableStateOf(false) }
            var pendingBackupImport by remember { mutableStateOf<String?>(null) }
            var pendingBackupCount by remember { mutableStateOf(0) }
            val backupDocumentLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                val json = pendingBackupJson
                pendingBackupJson = null
                if (uri == null || json == null) {
                    status = "Datensicherung abgebrochen."
                } else {
                    val saved = runCatching {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                            ?: error("Datei konnte nicht geöffnet werden.")
                    }
                    status = if (saved.isSuccess) "TRAKUNA-Datensicherung wurde gespeichert."
                    else "Datensicherung konnte nicht gespeichert werden: ${saved.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                }
            }
            val restoreDocumentLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri == null) {
                    status = "Wiederherstellung abgebrochen."
                } else {
                    val loaded = runCatching {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            readBoundedUtf8(input, ShipmentStore.MAX_BACKUP_BYTES)
                        } ?: error("Datei konnte nicht gelesen werden.")
                    }
                    if (loaded.isFailure) {
                        status = "Datei konnte nicht gelesen werden: ${loaded.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                    } else {
                        val json = loaded.getOrThrow()
                        when (val validation = shipmentStore.validateBackup(json)) {
                            is BackupValidation.Valid -> {
                                pendingBackupImport = json
                                pendingBackupCount = validation.recordCount
                            }
                            is BackupValidation.Invalid -> status = "Sicherung abgelehnt: ${validation.reason}"
                        }
                    }
                }
            }
            val shipmentRecords = remember(shipmentRevision) { shipmentStore.all() }
            val shipmentStorageIssue = remember(shipmentRevision) { shipmentStore.storageError() }

            val addShipment: (ShipmentFlow) -> Unit = { flow ->
                val result = shipmentStore.add(
                    number,
                    ShipmentRules.resolveCarrierLabel(carrierOverride, number),
                    flow
                )
                status = shipmentResultText(result)
                if (result is ShipmentUpdate.Saved) {
                    number = ""
                    carrierOverride = ""
                    showScanner = false
                }
                shipmentRevision++
            }

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding().padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Image(painter = painterResource(id = R.drawable.w_signature), contentDescription = "W-Signatur – Original", contentScale = ContentScale.Fit, modifier = Modifier.height(48.dp).fillMaxWidth())
                        Text("TRAKUNA", style = MaterialTheme.typography.headlineLarge, color = Forest)
                        Text("Paketshop", style = MaterialTheme.typography.titleLarge, color = Forest)
                        Text("Was möchtest du erledigen?", style = MaterialTheme.typography.bodyLarge)

                        Button(
                            onClick = { showScanner = !showScanner },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(if (showScanner) "Scanner schließen" else "Sendungsnummer scannen")
                        }
                        if (showScanner) {
                            ScannerPanel(
                                onDetected = { scanned ->
                                    showScanner = false
                                    if (scanned == null) {
                                        status = "Mehrere Codes erkannt. Bitte nur ein Paketlabel scannen oder die Nummer manuell eingeben."
                                    } else {
                                        number = scanned
                                        status = "Sendungsnummer erkannt. Bitte prüfen."
                                    }
                                },
                                onClose = { showScanner = false }
                            )
                        }
                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sendungsnummer") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            )
                        )
                        val detectedCarrier = ShipmentRules.identifyCarrier(number)
                        OutlinedTextField(
                            value = carrierOverride,
                            onValueChange = { carrierOverride = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Paketdienst (optional)") },
                            placeholder = { Text(detectedCarrier ?: "Wird bei eindeutiger Nummer erkannt") },
                            singleLine = true
                        )
                        Text(
                            if (carrierOverride.isNotBlank()) "Paketdienst: ${carrierOverride.trim()}"
                            else detectedCarrier?.let { "Erkannt: $it – bitte kurz prüfen" } ?: "Paketdienst bei Bedarf auswählen oder eingeben.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val canSaveShipment = shipmentStorageIssue == null &&
                            ShipmentRules.isValidTrackingNumber(normalizeTrackingNumber(number))
                        Button(
                            onClick = { addShipment(ShipmentFlow.INCOMING_FOR_CUSTOMER) },
                            enabled = canSaveShipment,
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) { Text("Paket für Kunden annehmen") }
                        Button(
                            onClick = { addShipment(ShipmentFlow.OUTGOING_FROM_CUSTOMER) },
                            enabled = canSaveShipment,
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) { Text("Versand vom Kunden annehmen") }
                        Button(
                            onClick = { addShipment(ShipmentFlow.CUSTOMER_RETURN) },
                            enabled = canSaveShipment,
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) { Text("Retoure annehmen") }
                        OutlinedButton(
                            onClick = { showShipmentLedger = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) { Text("Sendungen suchen / Abholung") }

                        if (showShipmentLedger) {
                            AlertDialog(
                                onDismissRequest = { showShipmentLedger = false },
                                title = { Text("Paketshop – Sendungen") },
                                text = {
                                    Column(
                                        modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Suche eine Sendung und dokumentiere die Abholung über die nächste Aktion.")
                                        Text("Gespeicherte Vorgänge", style = MaterialTheme.typography.titleSmall)
                                        if (shipmentStorageIssue == null) {
                                            Text("${shipmentRecords.size} von ${ShipmentStore.MAX_RECORDS} möglichen Einträgen belegt · ${shipmentRecords.count { it.archivedAt == null }} aktiv · ${shipmentRecords.count { it.archivedAt != null }} archiviert", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text("ACHTUNG: Lokaler Datenspeicher kann nicht gelesen werden: $shipmentStorageIssue. Neue Vorgänge und Exporte sind gesperrt. Du kannst eine vertrauenswürdige Sicherung wiederherstellen oder dich an den Support wenden.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                        }
                                        OutlinedButton(
                                            onClick = { showArchivedShipments = !showArchivedShipments; visibleRecordLimit = 100 },
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text(if (showArchivedShipments) "Aktive Vorgänge anzeigen" else "Archivierte Vorgänge anzeigen") }
                                        Text(if (showArchivedShipments) "Archiv – Vorgänge bleiben erhalten und können wiederhergestellt werden." else "Aktive Vorgänge – abgeschlossene Vorgänge können archiviert werden.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        OutlinedTextField(value = shipmentSearch, onValueChange = { shipmentSearch = it.take(120); visibleRecordLimit = 100 }, label = { Text("Sendungsnummer, Empfänger oder Paketdienst suchen") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedButton(onClick = { confirmBackupExport = true }, enabled = shipmentStorageIssue == null, modifier = Modifier.fillMaxWidth()) {
                                            Text("Vollständige Datensicherung erstellen")
                                        }
                                        OutlinedButton(onClick = {
                                            restoreDocumentLauncher.launch(arrayOf("application/json", "text/json", "application/octet-stream"))
                                        }, enabled = true, modifier = Modifier.fillMaxWidth()) { Text("Datensicherung wiederherstellen") }
                                        if (shipmentStorageIssue != null) {
                                            Text("Die gespeicherten Vorgänge werden nicht als leer angezeigt, weil der Speicher fehlerhaft gelesen wurde.", color = MaterialTheme.colorScheme.error)
                                        } else if (shipmentRecords.isEmpty()) {
                                            Text("Noch keine Paketshop-Vorgänge erfasst.")
                                        } else {
                                            val filteredRecords = shipmentRecords.filter { record ->
                                                (record.archivedAt != null) == showArchivedShipments &&
                                                    (shipmentSearch.isBlank() || listOf(record.trackingNumber, record.recipient, record.carrier, shipmentStatusLabel(record.status)).any { it.contains(shipmentSearch, ignoreCase = true) })
                                            }
                                            if (filteredRecords.isEmpty()) {
                                                Text("Keine passenden Sendungen gefunden.")
                                            }
                                            val visibleRecords = filteredRecords.take(visibleRecordLimit)
                                            visibleRecords.forEach { record ->
                                                Card(Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
                                                        Text("${shipmentFlowLabel(record.flow)} · ${shipmentStatusLabel(record.status)}")
                                                        if (record.archivedAt != null) Text("Archiviert: ${ShipmentRules.formatTimestamp(record.archivedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("Erfasst: ${ShipmentRules.formatTimestamp(record.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text("Zuletzt geändert: ${ShipmentRules.formatTimestamp(record.updatedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        if (record.carrier.isNotBlank()) Text(record.carrier, style = MaterialTheme.typography.bodySmall)
                                                        if (record.recipient.isNotBlank()) Text("Empfänger: ${record.recipient}", style = MaterialTheme.typography.bodySmall)
                                                        val canAdvance = record.archivedAt == null && !ShipmentRules.isTerminalStatus(record.status)
                                                        if (canAdvance) {
                                                            TextButton(onClick = { pendingAdvance = record }) {
                                                                Text(shipmentNextAction(record.flow, record.status))
                                                            }
                                                        }
                                                        if (record.archivedAt == null) {
                                                            if (ShipmentRules.isTerminalStatus(record.status)) {
                                                                TextButton(onClick = {
                                                                    val archived = shipmentStore.archive(record.id)
                                                                    status = if (archived) "Vorgang archiviert. Die Sendungsdaten und Historie bleiben erhalten." else "Nur abgeschlossene Vorgänge können archiviert werden."
                                                                    shipmentRevision++
                                                                }) { Text("Vorgang archivieren") }
                                                            }
                                                        } else {
                                                            TextButton(onClick = {
                                                                val restored = shipmentStore.restoreArchived(record.id)
                                                                status = if (restored) "Vorgang wurde aus dem Archiv wiederhergestellt." else "Vorgang konnte nicht wiederhergestellt werden."
                                                                shipmentRevision++
                                                            }) { Text("Wiederherstellen") }
                                                        }
                                                    }
                                                }
                                            }
                                            if (filteredRecords.size > visibleRecords.size) {
                                                TextButton(
                                                    onClick = { visibleRecordLimit = (visibleRecordLimit + 100).coerceAtMost(filteredRecords.size) },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) { Text("Weitere ${filteredRecords.size - visibleRecords.size} Treffer anzeigen") }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showShipmentLedger = false }) { Text("Schließen") }
                                }
                            )
                        }
                        if (confirmBackupExport) {
                            AlertDialog(
                                onDismissRequest = { confirmBackupExport = false },
                                title = { Text("Datensicherung erstellen?") },
                                text = {
                                    Text("Die Sicherungsdatei enthält Sendungsnummern, Empfängernamen und interne Notizen. Sie wird unverschlüsselt gespeichert. Wähle einen vertrauenswürdigen Speicherort und gib die Datei nicht weiter.")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        confirmBackupExport = false
                                        val exported = runCatching { shipmentStore.exportBackupJson() }
                                        if (exported.isSuccess) {
                                            pendingBackupJson = exported.getOrThrow()
                                            backupDocumentLauncher.launch("TRAKUNA-Sicherung.json")
                                        } else {
                                            status = exported.exceptionOrNull()?.message ?: "Datensicherung fehlgeschlagen."
                                        }
                                    }) { Text("Sicherung erstellen") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { confirmBackupExport = false }) { Text("Abbrechen") }
                                }
                            )
                        }
                        pendingBackupImport?.let { backupJson ->
                            AlertDialog(
                                onDismissRequest = { pendingBackupImport = null },
                                title = { Text("Datensicherung wiederherstellen?") },
                                text = {
                                    Text("Die Sicherung enthält $pendingBackupCount Vorgänge. Die aktuell auf diesem Gerät gespeicherten Vorgänge werden vollständig ersetzt. Erstelle bei Bedarf zuerst eine Sicherung des aktuellen Standes.")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val result = shipmentStore.restoreBackup(backupJson)
                                        status = when (result) {
                                            is BackupRestoreResult.Restored -> "${result.recordCount} Vorgänge wurden wiederhergestellt."
                                            is BackupRestoreResult.Rejected -> "Wiederherstellung fehlgeschlagen: ${result.reason}"
                                        }
                                        shipmentRevision++
                                        pendingBackupImport = null
                                    }) { Text("Ersetzen und wiederherstellen") }
                                },
                                dismissButton = { TextButton(onClick = { pendingBackupImport = null }) { Text("Abbrechen") } }
                            )
                        }
                        pendingAdvance?.let { record ->
                            val nextStatus = ShipmentRules.nextStatus(record.flow, record.status)
                            AlertDialog(
                                onDismissRequest = { pendingAdvance = null },
                                title = { Text("Statusänderung bestätigen?") },
                                text = {
                                    Text(
                                        when {
                                            nextStatus == ShipmentStatus.HANDED_TO_CARRIER ->
                                                "Soll ${record.trackingNumber} lokal als an den Paketdienst übergeben dokumentiert werden? Wichtig: TRAKUNA erhält dabei keine Bestätigung von DPD, GLS, Hermes oder UPS. Erst markieren, wenn die Übergabe im offiziellen Carrier-System abgeschlossen und dort quittiert wurde."
                                            nextStatus == ShipmentStatus.HANDED_TO_CUSTOMER ->
                                                "Soll ${record.trackingNumber} wirklich als ${nextStatus.let { shipmentStatusLabel(it) }} markiert werden? Bitte erst bestätigen, wenn die Sendung tatsächlich an den Kunden bzw. Bevollmächtigten ausgehändigt wurde."
                                            else ->
                                                "Soll ${record.trackingNumber} wirklich als ${nextStatus?.let { shipmentStatusLabel(it) }} markiert werden?"
                                        }
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val result = shipmentStore.advance(record.id)
                                        status = shipmentResultText(result)
                                        shipmentRevision++
                                        pendingAdvance = null
                                    }) { Text("Bestätigen") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { pendingAdvance = null }) { Text("Abbrechen") }
                                }
                            )
                        }


                    }
                }
            }
        }
    }
}


private fun shipmentFlowLabel(flow: ShipmentFlow): String = when (flow) {
    ShipmentFlow.INCOMING_FOR_CUSTOMER -> "Eingang für Kunden"
    ShipmentFlow.OUTGOING_FROM_CUSTOMER -> "Annahme vom Kunden"
    ShipmentFlow.CUSTOMER_RETURN -> "Kundenretoure"
}

private fun shipmentStatusLabel(status: ShipmentStatus): String = when (status) {
    ShipmentStatus.RECEIVED_BY_SHOP -> "Im Shop eingetroffen"
    ShipmentStatus.READY_FOR_PICKUP -> "Abholbereit"
    ShipmentStatus.HANDED_TO_CUSTOMER -> "An Kunden übergeben"
    ShipmentStatus.ACCEPTED_FROM_CUSTOMER -> "Vom Kunden angenommen"
    ShipmentStatus.RETURN_ACCEPTED -> "Retoure angenommen"
    ShipmentStatus.HANDED_TO_CARRIER -> "Lokal als an Paketdienst übergeben dokumentiert"
}

private fun shipmentNextAction(flow: ShipmentFlow, status: ShipmentStatus): String = when {
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER && status == ShipmentStatus.RECEIVED_BY_SHOP -> "Als abholbereit markieren"
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER -> "An Kunden übergeben"
    flow == ShipmentFlow.CUSTOMER_RETURN -> "Retoure – Übergabe lokal dokumentieren"
    else -> "Übergabe an Paketdienst lokal dokumentieren"
}

private fun shipmentResultText(result: ShipmentUpdate): String = when (result) {
    is ShipmentUpdate.Saved -> "Vorgang gespeichert: ${result.record.trackingNumber} · ${shipmentStatusLabel(result.record.status)}"
    is ShipmentUpdate.AlreadyExists -> "Diese Sendung ist bereits als ${shipmentFlowLabel(result.record.flow)} erfasst: ${result.record.trackingNumber} · ${shipmentStatusLabel(result.record.status)}"
    is ShipmentUpdate.Rejected -> result.reason
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
    if (!ShipmentRules.isValidTrackingNumber(number)) {
        return HandoffResult("Bitte eine gültige Sendungsnummer eingeben oder erneut scannen.")
    }

    // Persist the handoff before leaving TRAKUNA. Do not report success if Android
    // cannot confirm that the local cache was written.
    val preferences = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
    val handoffCache = SecureHandoffCache(context)
    val cached = handoffCache.save(number, carrier)
    if (!cached) {
        val detail = handoffCache.failureReason()
        return HandoffResult(
            "Sendungsnummer konnte nicht sicher gespeichert werden. " +
                (detail ?: "Bitte Gerätespeicher prüfen und erneut versuchen.")
        )
    }

    // Clipboard is only a fallback. Mark it sensitive on Android 13+ and never let
    // a clipboard service failure crash the parcel-shop workflow.
    val copied = runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: error("Zwischenablage ist nicht verfügbar.")
        val clip = ClipData.newPlainText("TRAKUNA Sendungsnummer", number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)
    }.isSuccess
    val clipboardMessage = if (copied) "Sendungsnummer zusätzlich kopiert." else "Zwischenablage nicht verfügbar; Nummer bleibt im TRAKUNA-Cache."

    val carrierSuffix = PartnerAppConfig.preferenceSuffixForCarrier(carrier)
    val hasCarrierSpecificMappings = PartnerAppConfig.priorityCarriers.any {
        preferences.contains("partner_package_$it")
    }
    val carrierSpecificPackage = carrierSuffix?.let { preferences.getString("partner_package_$it", null) }
    val packageName = PartnerAppConfig.resolveSelectedPackage(
        carrier,
        carrierSpecificPackage,
        hasCarrierSpecificMappings,
        preferences.getString("partner_package", null)
    )
    if (packageName.isNullOrBlank()) {
        val carrierLabel = carrier?.takeIf { it.isNotBlank() } ?: "den Paketdienst"
        return HandoffResult(
            "Nummer $number ist im TRAKUNA-Cache gesichert. $clipboardMessage " +
                "Ordne zuerst eine eigene Betriebssoftware für $carrierLabel zu."
        )
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: return HandoffResult(
            "Nummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Die konfigurierte Partner-App ($packageName) ist nicht installiert oder Android kann sie nicht starten."
        )

    // Contract for an adapted partner app: read EXTRA_TRACKING_NUMBER from the launch intent.
    launchIntent.putExtra(EXTRA_TRACKING_NUMBER, number)
    launchIntent.putExtra(EXTRA_CARRIER, carrier.orEmpty())
    launchIntent.putExtra(Intent.EXTRA_TEXT, number)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    return try {
        context.startActivity(launchIntent)
        HandoffResult("Sendungsnummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Partner-App wird geöffnet. Automatische Übernahme nur bei unterstützten Intent-Extras oder einer passenden Schnittstelle.")
    } catch (_: Exception) {
        HandoffResult("Nummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Die Partner-App ließ sich nicht öffnen.")
    }
}

const val EXTRA_TRACKING_NUMBER = "de.trakuna.app.extra.TRACKING_NUMBER"
const val EXTRA_CARRIER = "de.trakuna.app.extra.CARRIER"

/** Public accessor for a future in-process adapter or diagnostics screen. */
fun getCachedTrackingNumber(context: Context): String =
    SecureHandoffCache(context).trackingNumber()

private fun normalizeTrackingNumber(raw: String): String =
    ShipmentRules.normalizeTrackingNumber(raw)


private fun extractTrackingNumber(raw: String): String? =
    ShipmentRules.extractTrackingNumber(raw, MAX_SCAN_INPUT_CHARS)

private fun identifyCarrier(raw: String): String? =
    ShipmentRules.identifyCarrier(normalizeTrackingNumber(raw))

private fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun ScannerPanel(onDetected: (String?) -> Unit, onClose: () -> Unit) {
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

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
private fun CameraPreview(onDetected: (String?) -> Unit) {
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
            Barcode.FORMAT_CODE_128, Barcode.FORMAT_CODE_39, Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_CODABAR, Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_ITF, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_DATA_MATRIX, Barcode.FORMAT_PDF417,
            Barcode.FORMAT_AZTEC
        ).build()
        val scanner: BarcodeScanner = BarcodeScanning.getClient(options)
        val delivered = AtomicBoolean(false)
        val disposed = AtomicBoolean(false)
        // Camera frames can reveal different barcodes on the same label at slightly
        // different times. Collect candidates across several frames before choosing.
        val observedTrackingNumbers = ConcurrentHashMap.newKeySet<String>()
        val framesAfterFirstCandidate = AtomicInteger(0)
        val requiredConfirmationFrames = 12
        var provider: ProcessCameraProvider? = null
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed.get()) try {
                val cameraProvider = future.get()
                if (disposed.get()) return@addListener
                provider = cameraProvider
                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
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
                                        val values = barcodes.mapNotNull { barcode ->
                                            (barcode.rawValue?.takeIf { it.isNotBlank() } ?: barcode.displayValue)
                                                ?.let { extractTrackingNumber(it) }
                                        }.distinct()
                                        observedTrackingNumbers.addAll(values)
                                        if (observedTrackingNumbers.isNotEmpty() &&
                                            framesAfterFirstCandidate.incrementAndGet() >= requiredConfirmationFrames &&
                                            !disposed.get() && delivered.compareAndSet(false, true)
                                        ) {
                                            // Never guess when more than one distinct tracking ID
                                            // appeared during the confirmation window.
                                            onDetected(observedTrackingNumbers.singleOrNull())
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
                    cameraProvider.bindToLifecycle(activity as LifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
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
