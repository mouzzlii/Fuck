package de.trakuna.app

import android.content.Context
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.input.KeyboardCapitalization

private val Forest = Color(0xFF174B3A)
private const val DATAWEDGE_ACTION = "de.trakuna.app.DATAWEDGE_SCAN"
private const val DATAWEDGE_DATA_EXTRA = "com.symbol.datawedge.data_string"
private const val DATAWEDGE_LABEL_TYPE_EXTRA = "com.symbol.datawedge.label_type"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val handoffCache = remember(context) { SecureHandoffCache(context) }
            val initialHandoffRead = remember(handoffCache) { runCatching { handoffCache.trackingNumber() } }
            val initialHandoffNumber = initialHandoffRead.getOrDefault("")
            var number by rememberSaveable { mutableStateOf(initialHandoffNumber) }
            var showScanner by remember { mutableStateOf(false) }
            var scanDiagnostic by remember { mutableStateOf<ScanDiagnosticResult?>(null) }
            var showScanDiagnostics by remember { mutableStateOf(false) }
            var status by remember(handoffCache) {
                mutableStateOf(
                    if (initialHandoffNumber.isNotBlank()) "Gespeicherte Sendungsnummer wiederhergestellt."
                    else if (initialHandoffRead.isFailure) "Gespeicherte Sendungsnummer konnte nicht gelesen werden. Bitte erneut scannen oder manuell eingeben."
                    else "Sendungsnummer scannen oder manuell eingeben."
                )
            }
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
            var wifiConnected by remember { mutableStateOf(isWifiConnected(context)) }
            DisposableEffect(context) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        wifiConnected = isWifiConnected(context)
                        val cachedNumberRead = runCatching { handoffCache.trackingNumber() }
                        val cachedNumber = cachedNumberRead.getOrDefault("")
                        if (number.isBlank() && cachedNumber.isNotBlank()) {
                            number = cachedNumber
                            status = "Gespeicherte Sendungsnummer nach Rückkehr wiederhergestellt."
                        } else if (number.isBlank() && cachedNumberRead.isFailure) {
                            status = "Gespeicherte Sendungsnummer konnte nicht gelesen werden. Bitte erneut scannen oder manuell eingeben."
                        }
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
                        // Android 14+ exposes the original broadcast sender. Older Android versions
                        // cannot authenticate it in-app, so TRAKUNA rejects DataWedge input there.
                        val senderPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            runCatching { getSentFromPackage() }.getOrNull()
                        } else null
                        if (!isDataWedgeSenderAccepted(Build.VERSION.SDK_INT, senderPackage)) {
                            status = dataWedgeRejectionMessage(Build.VERSION.SDK_INT)
                            return
                        }
                        // Broadcasts are externally supplied; ignore a missing or wrongly typed
                        // extra instead of allowing a malformed parcel to crash the receiver.
                        val raw = runCatching { intent.getStringExtra(DATAWEDGE_DATA_EXTRA) }.getOrNull() ?: return
                        if (raw.length > MAX_SCAN_INPUT_CHARS) {
                            status = "Scaninhalt ist zu lang und wurde aus Sicherheitsgründen abgelehnt. Bitte Sendungsnummer manuell prüfen."
                            return
                        }
                        val labelType = runCatching { intent.getStringExtra(DATAWEDGE_LABEL_TYPE_EXTRA) }.getOrNull()
                        val supportedFormat = isAllowedDataWedgeLabelType(labelType)
                        val scanned = if (supportedFormat) extractTrackingNumber(raw) else null
                        scanDiagnostic = ScanDiagnosticResult(
                            observations = listOf(
                                ScanObservation(
                                    source = "Zebra DataWedge (Absender verifiziert)",
                                    format = labelType ?: "Format nicht mitgeliefert",
                                    rawValue = raw,
                                    displayValue = raw,
                                    extractedValue = scanned
                                )
                            ),
                            selectedValue = scanned
                        )
                        showScanDiagnostics = true
                        if (!supportedFormat) {
                            status = "Zebra-Barcodeformat fehlt oder wird nicht unterstützt. Scan wurde aus Sicherheitsgründen nicht übernommen. Bitte DataWedge-Profil prüfen."
                            return
                        }
                        if (scanned.isNullOrBlank()) {
                            status = "Zebra-Scan empfangen, aber keine unterstützte Sendungsnummer erkannt. Rohdaten stehen in der Scan-Diagnose zur Prüfung."
                            return
                        }
                        number = scanned
                        status = "Zebra-Scan empfangen. DataWedge-Absender verifiziert. Sendungsnummer bitte prüfen und den nächsten Schritt auswählen."
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
                                onDetected = { diagnostic ->
                                    showScanner = false
                                    scanDiagnostic = diagnostic
                                    showScanDiagnostics = true
                                    val scanned = diagnostic.selectedValue
                                    if (scanned == null) {
                                        status = "Scan erfasst, aber kein eindeutiger Sendungsnummern-Kandidat. Bitte Scan-Diagnose prüfen und Nummer manuell bestätigen."
                                    } else {
                                        number = scanned
                                        status = "Scan erkannt: $scanned. Bitte Sendungsnummer und Paketdienst prüfen. Es wurde noch nichts an eine andere App übergeben."
                                    }
                                },
                                onClose = { showScanner = false }
                            )
                        }
                        scanDiagnostic?.let { diagnostic ->
                            ScanDiagnosticsPanel(
                                diagnostic = diagnostic,
                                expanded = showScanDiagnostics,
                                onExpandedChange = { showScanDiagnostics = it },
                                onClear = {
                                    scanDiagnostic = null
                                    showScanDiagnostics = false
                                }
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
                                                ShipmentRecordCard(
                                                    record = record,
                                                    onAdvance = { pendingAdvance = record },
                                                    onHistory = { historyRecord = record },
                                                    onArchive = {
                                                        val archived = shipmentStore.archive(record.id)
                                                        status = if (archived) "Vorgang archiviert. Die Sendungsdaten und Historie bleiben erhalten." else "Nur abgeschlossene Vorgänge können archiviert werden."
                                                        shipmentRevision++
                                                    },
                                                    onEdit = {
                                                        editRecipient = record.recipient
                                                        editNote = record.note
                                                        editingRecord = record
                                                    },
                                                    onRestore = {
                                                        val restored = shipmentStore.restoreArchived(record.id)
                                                        status = if (restored) "Vorgang wurde aus dem Archiv wiederhergestellt." else "Vorgang konnte nicht wiederhergestellt werden."
                                                        shipmentRevision++
                                                    },
                                                    onDelete = { pendingDelete = record }
                                                )
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
                            CsvExportConfirmationDialog(
                                onConfirm = {
                                    confirmCsvExport = false
                                    val exported = runCatching { shipmentStore.exportCsv() }
                                    if (exported.isSuccess) {
                                        pendingCsv = exported.getOrThrow()
                                        csvDocumentLauncher.launch("TRAKUNA-Sendungen.csv")
                                    } else {
                                        status = exported.exceptionOrNull()?.message ?: "CSV-Export fehlgeschlagen."
                                    }
                                },
                                onDismiss = { confirmCsvExport = false }
                            )
                        }
                        if (confirmBackupExport) {
                            BackupExportConfirmationDialog(
                                onConfirm = {
                                    confirmBackupExport = false
                                    val exported = runCatching { shipmentStore.exportBackupJson() }
                                    if (exported.isSuccess) {
                                        pendingBackupJson = exported.getOrThrow()
                                        backupDocumentLauncher.launch("TRAKUNA-Sicherung.json")
                                    } else {
                                        status = exported.exceptionOrNull()?.message ?: "Datensicherung fehlgeschlagen."
                                    }
                                },
                                onDismiss = { confirmBackupExport = false }
                            )
                        }
                        pendingBackupImport?.let { backupJson ->
                            BackupImportConfirmationDialog(
                                recordCount = pendingBackupCount,
                                onConfirm = {
                                    val result = shipmentStore.restoreBackup(backupJson)
                                    status = when (result) {
                                        is BackupRestoreResult.Restored -> "${result.recordCount} Vorgänge wurden wiederhergestellt."
                                        is BackupRestoreResult.Rejected -> "Wiederherstellung fehlgeschlagen: ${result.reason}"
                                    }
                                    shipmentRevision++
                                    pendingBackupImport = null
                                },
                                onDismiss = { pendingBackupImport = null }
                            )
                        }
                        historyRecord?.let { record ->
                            ShipmentHistoryDialog(record = record, onDismiss = { historyRecord = null })
                        }
                        editingRecord?.let { record ->
                            EditShipmentDialog(
                                record = record,
                                recipient = editRecipient,
                                note = editNote,
                                onRecipientChange = { editRecipient = it },
                                onNoteChange = { editNote = it },
                                onSave = {
                                    val result = shipmentStore.updateDetails(record.id, editRecipient, editNote)
                                    status = shipmentResultText(result)
                                    shipmentRevision++
                                    editingRecord = null
                                },
                                onDismiss = { editingRecord = null }
                            )
                        }
                        pendingAdvance?.let { record ->
                            AdvanceShipmentDialog(
                                record = record,
                                onConfirm = {
                                    val result = shipmentStore.advance(record.id, expectedStatus = record.status)
                                    status = shipmentResultText(result)
                                    shipmentRevision++
                                    pendingAdvance = null
                                },
                                onDismiss = { pendingAdvance = null }
                            )
                        }
                        pendingDelete?.let { record ->
                            DeleteShipmentDialog(
                                record = record,
                                onConfirm = {
                                    val removed = shipmentStore.remove(record.id)
                                    status = if (removed) "Vorgang und lokale Historie wurden endgültig gelöscht." else "Vorgang konnte nicht gelöscht werden. Datenspeicher prüfen."
                                    shipmentRevision++
                                    pendingDelete = null
                                },
                                onDismiss = { pendingDelete = null }
                            )
                        }
                        if (showPartnerPicker) {
                            val candidates = if (showAllApps) installedApps else installedApps.filter { it.looksLikeCarrierApp }
                            val partnerPrefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
                            val currentPackageKey = "partner_package_$partnerPickerCarrier"
                            PartnerAppPickerDialog(
                                carrier = partnerPickerCarrier,
                                candidates = candidates,
                                showCarrierMenu = showPartnerCarrierMenu,
                                showAllApps = showAllApps,
                                hasCurrentMapping = partnerPrefs.getString(currentPackageKey, null) != null,
                                onCarrierMenuChange = { showPartnerCarrierMenu = it },
                                onCarrierSelected = {
                                    partnerPickerCarrier = it
                                    showPartnerCarrierMenu = false
                                    showAllApps = false
                                },
                                onAppSelected = { app ->
                                    val saved = partnerPrefs.edit()
                                        .putString(currentPackageKey, app.packageName)
                                        .putString("partner_label_$partnerPickerCarrier", app.label)
                                        .remove("partner_package")
                                        .remove("partner_label")
                                        .commit()
                                    if (saved) {
                                        status = "$partnerPickerCarrier wurde der App ${app.label} zugeordnet. Eine automatische Übernahme oder Buchung ist dadurch nicht bestätigt."
                                        showPartnerPicker = false
                                    } else {
                                        status = "Zuordnung konnte nicht dauerhaft gespeichert werden. Bitte Gerätespeicher prüfen und erneut versuchen."
                                    }
                                },
                                onShowAllApps = { showAllApps = true },
                                onRemoveMapping = {
                                    val cleared = partnerPrefs.edit()
                                        .remove(currentPackageKey)
                                        .remove("partner_label_$partnerPickerCarrier")
                                        .commit()
                                    status = if (cleared) "Zuordnung für $partnerPickerCarrier wurde entfernt."
                                    else "Zuordnung konnte nicht entfernt werden. Bitte erneut versuchen."
                                },
                                onDismiss = {
                                    showPartnerPicker = false
                                    showPartnerCarrierMenu = false
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









private fun isWifiConnected(context: Context): Boolean {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = connectivity.activeNetwork ?: return false
    val capabilities = connectivity.getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}



