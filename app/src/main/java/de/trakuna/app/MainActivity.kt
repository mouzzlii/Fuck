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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Build
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
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
            var showPartnerPicker by remember { mutableStateOf(false) }
            var partnerPickerCarrier by remember { mutableStateOf("DPD") }
            var showPartnerCarrierMenu by remember { mutableStateOf(false) }
            var showAllApps by remember { mutableStateOf(false) }
            var installedApps by remember { mutableStateOf<List<PartnerCandidate>>(emptyList()) }
            var showShipmentLedger by remember { mutableStateOf(false) }
            var shipmentRevision by remember { mutableStateOf(0) }
            var recipient by remember { mutableStateOf("") }
            var carrierOverride by remember { mutableStateOf("") }
            var showCarrierMenu by remember { mutableStateOf(false) }
            var shipmentNote by remember { mutableStateOf("") }
            var shipmentSearch by remember { mutableStateOf("") }
            var showArchivedShipments by remember { mutableStateOf(false) }
            var visibleRecordLimit by remember { mutableStateOf(100) }
            var pendingAdvance by remember { mutableStateOf<ShipmentRecord?>(null) }
            var pendingDelete by remember { mutableStateOf<ShipmentRecord?>(null) }
            var editingRecord by remember { mutableStateOf<ShipmentRecord?>(null) }
            var historyRecord by remember { mutableStateOf<ShipmentRecord?>(null) }
            var editRecipient by remember { mutableStateOf("") }
            var editNote by remember { mutableStateOf("") }
            val context = LocalContext.current
            var wifiConnected by remember { mutableStateOf(isWifiConnected(context)) }
            DisposableEffect(context) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        wifiConnected = isWifiConnected(context)
                    }
                }
                this@MainActivity.lifecycle.addObserver(observer)
                onDispose { this@MainActivity.lifecycle.removeObserver(observer) }
            }
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
            var pendingCsv by remember { mutableStateOf<String?>(null) }
            var pendingBackupJson by remember { mutableStateOf<String?>(null) }
            var confirmBackupExport by remember { mutableStateOf(false) }
            var confirmCsvExport by remember { mutableStateOf(false) }
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
            val csvDocumentLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                val csv = pendingCsv
                pendingCsv = null
                if (uri == null || csv == null) {
                    status = "CSV-Export abgebrochen."
                } else {
                    val saved = runCatching {
                        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                            it.write("\uFEFF")
                            it.write(csv)
                        } ?: error("Datei konnte nicht geöffnet werden.")
                    }
                    status = if (saved.isSuccess) "CSV-Datei wurde gespeichert."
                    else "CSV-Datei konnte nicht gespeichert werden: ${saved.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                }
            }
            val shipmentRecords = remember(shipmentRevision) { shipmentStore.all() }
            val shipmentStorageIssue = remember(shipmentRevision) { shipmentStore.storageError() }

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding().padding(20.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text("TRAKUNA", style = MaterialTheme.typography.headlineLarge, color = Forest)
                        Text("SCAN-ZENTRALE", style = MaterialTheme.typography.labelLarge)
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    if (wifiConnected) "WLAN verbunden" else "Kein WLAN verbunden",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (wifiConnected) Forest else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    if (wifiConnected)
                                        "Dieses TRAKUNA-Gerät ist mit einem WLAN verbunden. Ob die anderen Handgeräte im selben Netzwerk erreichbar sind, ist damit noch nicht geprüft."
                                    else
                                        "Dieses Gerät ist derzeit nicht mit einem WLAN verbunden. Der lokale Scanbetrieb funktioniert weiterhin.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Button(
                            onClick = { showScanner = !showScanner },
                            modifier = Modifier.fillMaxWidth().height(58.dp)
                        ) {
                            Text(if (showScanner) "Scanner schließen" else "Barcode / QR-Code scannen")
                        }
                        if (showScanner) {
                            ScannerPanel(
                                onDetected = { scanned ->
                                    showScanner = false
                                    if (scanned == null) {
                                        status = "Mehrere unterschiedliche Codes erkannt. Bitte nur ein Paketlabel gleichzeitig scannen oder die Sendungsnummer manuell eingeben."
                                    } else {
                                        number = scanned
                                        status = "Scan erkannt: $scanned. Bitte Sendungsnummer und Paketdienst prüfen. Es wurde noch nichts an eine andere App übergeben."
                                    }
                                },
                                onClose = { showScanner = false }
                            )
                        }
                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Sendungsnummer manuell eingeben") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            )
                        )
                        Text("Start-Paketdienste", style = MaterialTheme.typography.titleSmall, color = Forest)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showCarrierMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (carrierOverride.isBlank()) "Paketdienst auswählen (DPD, GLS, Hermes, UPS)"
                                    else "Ausgewählt: ${carrierOverride}"
                                )
                            }
                            DropdownMenu(
                                expanded = showCarrierMenu,
                                onDismissRequest = { showCarrierMenu = false }
                            ) {
                                listOf("DPD", "GLS", "Hermes", "UPS", "Ohne Auswahl").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            carrierOverride = if (option == "Ohne Auswahl") "" else option
                                            showCarrierMenu = false
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = carrierOverride,
                            onValueChange = { carrierOverride = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Paketdienst (manuell änderbar)") },
                            placeholder = { Text("DPD, GLS, Hermes oder UPS") },
                            supportingText = { Text("Die Auswahl kennzeichnet nur den Vorgang. Eine offizielle Carrier-Buchung oder Schnittstelle ist damit nicht verbunden.") },
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val carrier = ShipmentRules.resolveCarrierLabel(carrierOverride, number)
                                val result = handoffToPartnerApp(context, number, carrier.ifBlank { null })
                                status = result.message
                            },
                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("An Partner-App übergeben")
                        }
                        OutlinedButton(
                            onClick = { showShipmentLedger = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Paketshop-Sendungen verwalten") }
                        OutlinedButton(
                            onClick = {
                                val suggestedCarrier = ShipmentRules.resolveCarrierLabel(carrierOverride, number)
                                partnerPickerCarrier = PartnerAppConfig.priorityCarriers.firstOrNull {
                                    it == suggestedCarrier
                                } ?: "DPD"
                                installedApps = findLaunchableApps(context)
                                showAllApps = false
                                showPartnerPicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Betriebssoftware pro Paketdienst zuordnen") }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            val partnerPrefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
                            PartnerAppConfig.priorityCarriers.forEach { carrier ->
                                val label = partnerPrefs.getString("partner_label_$carrier", null)
                                Text(
                                    "$carrier: ${label ?: "noch nicht zugeordnet"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Forest
                                )
                            }
                            if (PartnerAppConfig.priorityCarriers.none { partnerPrefs.contains("partner_package_$it") }) {
                                partnerPrefs.getString("partner_label", null)?.let {
                                    Text(
                                        "Alte gemeinsame Zuordnung vorhanden: $it. Sie gilt vorübergehend für alle vier Paketdienste, bis Einzelzuordnungen gespeichert werden.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(status, style = MaterialTheme.typography.bodyMedium, color = Forest)
                        if (showShipmentLedger) {
                            AlertDialog(
                                onDismissRequest = { showShipmentLedger = false },
                                title = { Text("Paketshop – Sendungen") },
                                text = {
                                    Column(
                                        modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Neuen Vorgang mit der oben eingegebenen Sendungsnummer erfassen:")
                                        OutlinedTextField(value = recipient, onValueChange = { recipient = it.take(100) }, label = { Text("Empfänger / Kundenname (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = shipmentNote, onValueChange = { shipmentNote = it.take(500) }, label = { Text("Notiz (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                                        if (shipmentStorageIssue != null) {
                                            Text(
                                                "Erfassung derzeit gesperrt: $shipmentStorageIssue",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        } else if (normalizeTrackingNumber(number).isBlank()) {
                                            Text(
                                                "Bitte zuerst oben eine Sendungsnummer eingeben. Danach wird die passende Erfassungs-Schaltfläche aktiv.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                val result = shipmentStore.add(number, ShipmentRules.resolveCarrierLabel(carrierOverride, number), ShipmentFlow.INCOMING_FOR_CUSTOMER, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                if (result is ShipmentUpdate.Saved) {
                                                    recipient = ""
                                                    carrierOverride = ""
                                                    shipmentNote = ""
                                                    showShipmentLedger = false
                                                }
                                                shipmentRevision++
                                            },
                                            enabled = shipmentStorageIssue == null && normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Paket für Kunden annehmen") }
                                        Button(
                                            onClick = {
                                                val result = shipmentStore.add(number, ShipmentRules.resolveCarrierLabel(carrierOverride, number), ShipmentFlow.OUTGOING_FROM_CUSTOMER, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                if (result is ShipmentUpdate.Saved) {
                                                    recipient = ""
                                                    carrierOverride = ""
                                                    shipmentNote = ""
                                                    showShipmentLedger = false
                                                }
                                                shipmentRevision++
                                            },
                                            enabled = shipmentStorageIssue == null && normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Sendung vom Kunden annehmen") }
                                        Button(
                                            onClick = {
                                                val result = shipmentStore.add(number, ShipmentRules.resolveCarrierLabel(carrierOverride, number), ShipmentFlow.CUSTOMER_RETURN, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                if (result is ShipmentUpdate.Saved) {
                                                    recipient = ""
                                                    carrierOverride = ""
                                                    shipmentNote = ""
                                                    showShipmentLedger = false
                                                }
                                                shipmentRevision++
                                            },
                                            enabled = shipmentStorageIssue == null && normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Retoure annehmen") }
                                        Text(
                                            status,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        HorizontalDivider()
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
                                        OutlinedButton(onClick = { confirmCsvExport = true }, enabled = shipmentStorageIssue == null, modifier = Modifier.fillMaxWidth()) { Text("CSV-Datei exportieren") }
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
                                                        if (record.note.isNotBlank()) Text("Notiz: ${record.note}", style = MaterialTheme.typography.bodySmall)
                                                        val canAdvance = record.archivedAt == null && !ShipmentRules.isTerminalStatus(record.status)
                                                        if (canAdvance) {
                                                            TextButton(onClick = { pendingAdvance = record }) {
                                                                Text(shipmentNextAction(record.flow, record.status))
                                                            }
                                                        }
                                                        TextButton(onClick = { historyRecord = record }) { Text("Vorgangshistorie anzeigen (${record.history.size})") }
                                                        if (record.archivedAt == null) {
                                                            if (ShipmentRules.isTerminalStatus(record.status)) {
                                                                TextButton(onClick = {
                                                                    val archived = shipmentStore.archive(record.id)
                                                                    status = if (archived) "Vorgang archiviert. Die Sendungsdaten und Historie bleiben erhalten." else "Nur abgeschlossene Vorgänge können archiviert werden."
                                                                    shipmentRevision++
                                                                }) { Text("Vorgang archivieren") }
                                                            }
                                                            TextButton(onClick = { editRecipient = record.recipient; editNote = record.note; editingRecord = record }) { Text("Details bearbeiten") }
                                                        } else {
                                                            TextButton(onClick = {
                                                                val restored = shipmentStore.restoreArchived(record.id)
                                                                status = if (restored) "Vorgang wurde aus dem Archiv wiederhergestellt." else "Vorgang konnte nicht wiederhergestellt werden."
                                                                shipmentRevision++
                                                            }) { Text("Wiederherstellen") }
                                                            TextButton(onClick = { pendingDelete = record }) { Text("Endgültig löschen …", color = MaterialTheme.colorScheme.error) }
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
                        if (confirmCsvExport) {
                            AlertDialog(
                                onDismissRequest = { confirmCsvExport = false },
                                title = { Text("CSV-Datei exportieren?") },
                                text = {
                                    Text("Die CSV-Datei enthält Sendungsnummern, Empfängernamen, interne Notizen und die lokal gespeicherte Vorgangshistorie. Sie wird unverschlüsselt gespeichert. Die Historie ist kein manipulationssicherer Prüfbeleg. Wähle einen geschützten Speicherort und gib die Datei nur an berechtigte Personen weiter.")
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        confirmCsvExport = false
                                        val exported = runCatching { shipmentStore.exportCsv() }
                                        if (exported.isSuccess) {
                                            pendingCsv = exported.getOrThrow()
                                            csvDocumentLauncher.launch("TRAKUNA-Sendungen.csv")
                                        } else {
                                            status = exported.exceptionOrNull()?.message ?: "CSV-Export fehlgeschlagen."
                                        }
                                    }) { Text("CSV erstellen") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { confirmCsvExport = false }) { Text("Abbrechen") }
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
                        historyRecord?.let { record ->
                            AlertDialog(
                                onDismissRequest = { historyRecord = null },
                                title = { Text("Vorgangshistorie") },
                                text = {
                                    Column(
                                        modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
                                        if (record.history.isEmpty()) {
                                            Text("Für diesen älteren Vorgang liegen noch keine einzelnen Verlaufseinträge vor.")
                                        } else {
                                            record.history.asReversed().forEach { event ->
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        when (event.type) {
                                                            ShipmentEventType.CREATED -> "Vorgang angelegt"
                                                            ShipmentEventType.STATUS_CHANGED -> "Status geändert: ${event.fromStatus?.let(::shipmentStatusLabel) ?: "unbekannt"} → ${event.toStatus?.let(::shipmentStatusLabel) ?: "unbekannt"}"
                                                            ShipmentEventType.DETAILS_UPDATED -> "Empfänger oder Notiz bearbeitet"
                                                            ShipmentEventType.ARCHIVED -> "Vorgang archiviert"
                                                            ShipmentEventType.RESTORED -> "Vorgang aus Archiv wiederhergestellt"
                                                        },
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(ShipmentRules.formatTimestamp(event.at), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                                HorizontalDivider()
                                            }
                                        }
                                        Text("Die Historie wird lokal auf diesem Gerät gespeichert. Sie ist kein manipulationssicheres, zentrales Prüfprotokoll.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                confirmButton = { TextButton(onClick = { historyRecord = null }) { Text("Schließen") } }
                            )
                        }
                        editingRecord?.let { record ->
                            AlertDialog(
                                onDismissRequest = { editingRecord = null },
                                title = { Text("Sendungsdetails bearbeiten") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
                                        OutlinedTextField(value = editRecipient, onValueChange = { editRecipient = it.take(100) }, label = { Text("Empfänger / Kundenname") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = editNote, onValueChange = { editNote = it.take(500) }, label = { Text("Interne Notiz") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val result = shipmentStore.updateDetails(record.id, editRecipient, editNote)
                                        status = shipmentResultText(result)
                                        shipmentRevision++
                                        editingRecord = null
                                    }) { Text("Speichern") }
                                },
                                dismissButton = { TextButton(onClick = { editingRecord = null }) { Text("Abbrechen") } }
                            )
                        }
                        pendingAdvance?.let { record ->
                            val nextStatus = ShipmentRules.nextStatus(record.flow, record.status)
                            val isPhysicalHandover = nextStatus == ShipmentStatus.HANDED_TO_CUSTOMER ||
                                nextStatus == ShipmentStatus.HANDED_TO_CARRIER
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
                        pendingDelete?.let { record ->
                            AlertDialog(
                                onDismissRequest = { pendingDelete = null },
                                title = { Text("Vorgang endgültig löschen?") },
                                text = {
                                    Text(
                                        "Achtung: ${record.trackingNumber} wird dauerhaft von diesem Gerät entfernt. " +
                                            "Auch die lokal gespeicherte Vorgangshistorie geht dabei verloren. " +
                                            "Diese Löschung wird nicht in einem manipulationssicheren Prüfprotokoll festgehalten und kann nicht rückgängig gemacht werden. " +
                                            "Wenn du den Vorgang noch dokumentieren musst, brich ab und erstelle zuerst eine Datensicherung."
                                    )
                                },
                                confirmButton = { TextButton(onClick = {
                                    val removed = shipmentStore.remove(record.id)
                                    status = if (removed) "Vorgang und lokale Historie wurden endgültig gelöscht." else "Vorgang konnte nicht gelöscht werden. Datenspeicher prüfen."
                                    shipmentRevision++
                                    pendingDelete = null
                                }) { Text("Endgültig löschen") } },
                                dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Abbrechen") } }
                            )
                        }
                        if (showPartnerPicker) {
                            val candidates = if (showAllApps) installedApps else installedApps.filter { it.looksLikeCarrierApp }
                            val partnerPrefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
                            val currentPackageKey = "partner_package_$partnerPickerCarrier"
                            val currentLabelKey = "partner_label_$partnerPickerCarrier"
                            AlertDialog(
                                onDismissRequest = {
                                    showPartnerPicker = false
                                    showPartnerCarrierMenu = false
                                },
                                title = { Text("Betriebssoftware für $partnerPickerCarrier zuordnen") },
                                text = {
                                    Column(modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box {
                                            OutlinedButton(
                                                onClick = { showPartnerCarrierMenu = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) { Text("Paketdienst: $partnerPickerCarrier") }
                                            DropdownMenu(
                                                expanded = showPartnerCarrierMenu,
                                                onDismissRequest = { showPartnerCarrierMenu = false }
                                            ) {
                                                PartnerAppConfig.priorityCarriers.forEach { carrier ->
                                                    DropdownMenuItem(
                                                        text = { Text(carrier) },
                                                        onClick = {
                                                            partnerPickerCarrier = carrier
                                                            showPartnerCarrierMenu = false
                                                            showAllApps = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        Text("Wähle die installierte App, die zu diesem Paketdienst gehört. Die Zuordnung wird nur auf diesem Gerät gespeichert.")
                                        Text(if (candidates.isEmpty()) "Keine passende Paketdienst-App gefunden. Du kannst alle installierten Apps anzeigen." else "Installierte Apps:")
                                        candidates.take(30).forEach { app ->
                                            TextButton(onClick = {
                                                val saved = partnerPrefs.edit()
                                                    .putString(currentPackageKey, app.packageName)
                                                    .putString(currentLabelKey, app.label)
                                                    .remove("partner_package")
                                                    .remove("partner_label")
                                                    .commit()
                                                if (saved) {
                                                    status = "$partnerPickerCarrier wurde der App ${app.label} zugeordnet. Eine automatische Übernahme oder Buchung ist dadurch nicht bestätigt."
                                                    showPartnerPicker = false
                                                } else {
                                                    status = "Zuordnung konnte nicht dauerhaft gespeichert werden. Bitte Gerätespeicher prüfen und erneut versuchen."
                                                }
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                Text("${app.label}  ·  ${app.packageName}")
                                            }
                                        }
                                        if (!showAllApps) TextButton(onClick = { showAllApps = true }) { Text("Alle installierten Apps anzeigen") }
                                        if (partnerPrefs.getString(currentPackageKey, null) != null) {
                                            TextButton(onClick = {
                                                val cleared = partnerPrefs.edit()
                                                    .remove(currentPackageKey)
                                                    .remove(currentLabelKey)
                                                    .commit()
                                                status = if (cleared) "Zuordnung für $partnerPickerCarrier wurde entfernt."
                                                else "Zuordnung konnte nicht entfernt werden. Bitte erneut versuchen."
                                            }, modifier = Modifier.fillMaxWidth()) {
                                                Text("Zuordnung für $partnerPickerCarrier entfernen")
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showPartnerPicker = false
                                        showPartnerCarrierMenu = false
                                    }) { Text("Schließen") }
                                }
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Sendungsnummern werden lokal auf diesem Gerät gespeichert. Eine automatische Übernahme in Partner-Software klappt nur, wenn diese die übergebenen Intent-Extras oder eine passende Schnittstelle unterstützt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
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

private fun isWifiConnected(context: Context): Boolean {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = connectivity.activeNetwork ?: return false
    val capabilities = connectivity.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

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
