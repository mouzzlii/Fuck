package de.trakuna.app

import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
                        val scanned = if (supportedFormat) extractTrackingNumber(raw, rejectRetailProductBarcode = isDataWedgeRetailProductBarcode(labelType)) else null
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
            var pendingBackupLegacy by remember { mutableStateOf(false) }
            var pendingEncryptedBackup by remember { mutableStateOf<String?>(null) }
            var backupExportPassword by remember { mutableStateOf("") }
            var backupExportPasswordConfirm by remember { mutableStateOf("") }
            var backupImportPassword by remember { mutableStateOf("") }
            var backupBusy by remember { mutableStateOf(false) }
            val backupScope = rememberCoroutineScope()
            val backupDocumentLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/octet-stream")
            ) { uri ->
                val encrypted = pendingBackupJson
                pendingBackupJson = null
                if (uri == null || encrypted == null) {
                    status = "Datensicherung abgebrochen."
                } else {
                    backupBusy = true
                    status = "Datensicherung wird gespeichert…"
                    backupScope.launch {
                        try {
                            val saved = withContext(Dispatchers.IO) {
                                runCatching {
                                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(encrypted) }
                                        ?: error("Datei konnte nicht geöffnet werden.")
                                }
                            }
                            status = if (saved.isSuccess) "TRAKUNA-Datensicherung wurde gespeichert."
                            else "Datensicherung konnte nicht gespeichert werden: ${saved.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                        } finally {
                            backupBusy = false
                        }
                    }
                }
            }
            val restoreDocumentLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri == null) {
                    status = "Wiederherstellung abgebrochen."
                } else {
                    backupBusy = true
                    status = "Sicherungsdatei wird geprüft…"
                    backupScope.launch {
                        try {
                            val loaded = withContext(Dispatchers.IO) {
                                runCatching {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        readBoundedUtf8(input, BackupCrypto.MAX_ENCRYPTED_BACKUP_BYTES)
                                    } ?: error("Datei konnte nicht gelesen werden.")
                                }
                            }
                            if (loaded.isFailure) {
                                status = "Datei konnte nicht gelesen werden: ${loaded.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                            } else {
                                val raw = loaded.getOrThrow()
                                if (BackupCrypto.isEncryptedBackup(raw)) {
                                    pendingEncryptedBackup = raw
                                    backupImportPassword = ""
                                } else {
                                    val validation = withContext(Dispatchers.Default) { shipmentStore.validateBackup(raw) }
                                    when (validation) {
                                        is BackupValidation.Valid -> {
                                            pendingBackupImport = raw
                                            pendingBackupCount = validation.recordCount
                                            pendingBackupLegacy = true
                                        }
                                        is BackupValidation.Invalid -> status = "Sicherung abgelehnt: ${validation.reason}"
                                    }
                                }
                            }
                        } finally {
                            backupBusy = false
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
                        Image(painter = painterResource(id = R.drawable.w_signature), contentDescription = "W-Signatur – Original", contentScale = ContentScale.Fit, modifier = Modifier.height(48.dp).fillMaxWidth())
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

                        OutlinedButton(
                            onClick = { showScanDiagnostics = !showScanDiagnostics },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showScanDiagnostics) "Scan-Diagnose schließen" else "Scan-Diagnose öffnen")
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
                        if (showScanDiagnostics) {
                            ScanDiagnosticsPanel(
                                diagnostic = scanDiagnostic,
                                expanded = showScanDiagnostics,
                                onExpandedChange = { showScanDiagnostics = it },
                                onClear = {
                                    scanDiagnostic = null
                                    showScanDiagnostics = false
                                },
                                onCopyResult = { copied ->
                                    status = if (copied) "Scan-Diagnose wurde in die Zwischenablage kopiert."
                                    else "Kopieren fehlgeschlagen. Bitte Zwischenablage bzw. Geräteeinstellungen prüfen."
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
                                        ShipmentIntakeForm(
                                            number = number,
                                            recipient = recipient,
                                            note = shipmentNote,
                                            status = status,
                                            storageIssue = shipmentStorageIssue,
                                            onRecipientChange = { recipient = it },
                                            onNoteChange = { shipmentNote = it },
                                            onCreate = { flow ->
                                                val result = shipmentStore.add(
                                                    number,
                                                    ShipmentRules.resolveCarrierLabel(carrierOverride, number),
                                                    flow,
                                                    recipient,
                                                    shipmentNote
                                                )
                                                status = shipmentResultText(result)
                                                if (result is ShipmentUpdate.Saved) {
                                                    recipient = ""
                                                    carrierOverride = ""
                                                    shipmentNote = ""
                                                    showShipmentLedger = false
                                                }
                                                shipmentRevision++
                                            }
                                        )
                                        ShipmentLedgerList(
                                            records = shipmentRecords,
                                            storageIssue = shipmentStorageIssue,
                                            archived = showArchivedShipments,
                                            search = shipmentSearch,
                                            visibleRecordLimit = visibleRecordLimit,
                                            actions = ShipmentLedgerActions(
                                                onToggleArchived = {
                                                    showArchivedShipments = !showArchivedShipments
                                                    visibleRecordLimit = 100
                                                },
                                                onSearchChange = {
                                                    shipmentSearch = it.take(120)
                                                    visibleRecordLimit = 100
                                                },
                                                onVisibleLimitChange = { visibleRecordLimit = it },
                                                onCsvExport = { confirmCsvExport = true },
                                                onBackupExport = { if (!backupBusy) confirmBackupExport = true },
                                                onBackupImport = {
                                                    if (!backupBusy) restoreDocumentLauncher.launch(arrayOf("application/json", "text/json", "application/octet-stream"))
                                                },
                                                onAdvance = { pendingAdvance = it },
                                                onHistory = { historyRecord = it },
                                                onArchive = { record ->
                                                    val archived = shipmentStore.archive(record.id)
                                                    status = if (archived) "Vorgang archiviert. Die Sendungsdaten und Historie bleiben erhalten." else "Nur abgeschlossene Vorgänge können archiviert werden."
                                                    shipmentRevision++
                                                },
                                                onEdit = { record ->
                                                    editRecipient = record.recipient
                                                    editNote = record.note
                                                    editingRecord = record
                                                },
                                                onRestore = { record ->
                                                    val restored = shipmentStore.restoreArchived(record.id)
                                                    status = if (restored) "Vorgang wurde aus dem Archiv wiederhergestellt." else "Vorgang konnte nicht wiederhergestellt werden."
                                                    shipmentRevision++
                                                },
                                                onDelete = { pendingDelete = it }
                                            )
                                        )
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
                                password = backupExportPassword,
                                passwordConfirm = backupExportPasswordConfirm,
                                onPasswordChange = { backupExportPassword = it },
                                onPasswordConfirmChange = { backupExportPasswordConfirm = it },
                                onConfirm = {
                                    val passwordChars = backupExportPassword.toCharArray()
                                    confirmBackupExport = false
                                    backupExportPassword = ""
                                    backupExportPasswordConfirm = ""
                                    backupBusy = true
                                    status = "Sicherung wird verschlüsselt…"
                                    backupScope.launch {
                                        try {
                                            val exported = withContext(Dispatchers.Default) {
                                                runCatching { BackupCrypto.encrypt(shipmentStore.exportBackupJson(), passwordChars) }
                                            }
                                            if (exported.isSuccess) {
                                                pendingBackupJson = exported.getOrThrow()
                                                backupDocumentLauncher.launch("TRAKUNA-Sicherung.trakuna")
                                            } else {
                                                status = exported.exceptionOrNull()?.message ?: "Verschlüsselte Datensicherung fehlgeschlagen."
                                            }
                                        } finally {
                                            passwordChars.fill('\u0000')
                                            backupBusy = false
                                        }
                                    }
                                },
                                onDismiss = {
                                    confirmBackupExport = false
                                    backupExportPassword = ""
                                    backupExportPasswordConfirm = ""
                                }
                            )
                        }
                        pendingEncryptedBackup?.let { encryptedBackup ->
                            EncryptedBackupImportPasswordDialog(
                                password = backupImportPassword,
                                onPasswordChange = { backupImportPassword = it },
                                busy = backupBusy,
                                onConfirm = {
                                    val passwordChars = backupImportPassword.toCharArray()
                                    backupImportPassword = ""
                                    backupBusy = true
                                    status = "Sicherung wird entschlüsselt und geprüft…"
                                    backupScope.launch {
                                        try {
                                            val decrypted = withContext(Dispatchers.Default) {
                                                runCatching { BackupCrypto.decrypt(encryptedBackup, passwordChars) }
                                            }
                                            if (decrypted.isFailure) {
                                                status = "Sicherung konnte nicht entschlüsselt werden. Passwort und Datei prüfen."
                                            } else {
                                                val plain = decrypted.getOrThrow()
                                                val validation = withContext(Dispatchers.Default) { shipmentStore.validateBackup(plain) }
                                                when (validation) {
                                                    is BackupValidation.Valid -> {
                                                        pendingBackupImport = plain
                                                        pendingBackupCount = validation.recordCount
                                                        pendingBackupLegacy = false
                                                        pendingEncryptedBackup = null
                                                    }
                                                    is BackupValidation.Invalid -> {
                                                        pendingEncryptedBackup = null
                                                        status = "Sicherung abgelehnt: ${validation.reason}"
                                                    }
                                                }
                                            }
                                        } finally {
                                            passwordChars.fill('\u0000')
                                            backupBusy = false
                                        }
                                    }
                                },
                                onDismiss = {
                                    pendingEncryptedBackup = null
                                    backupImportPassword = ""
                                }
                            )
                        }
                        pendingBackupImport?.let { backupJson ->
                            BackupImportConfirmationDialog(
                                recordCount = pendingBackupCount,
                                isLegacy = pendingBackupLegacy,
                                onConfirm = {
                                    if (!backupBusy) {
                                        backupBusy = true
                                        status = "Wiederherstellung läuft…"
                                        backupScope.launch {
                                            try {
                                                val result = withContext(Dispatchers.IO) { shipmentStore.restoreBackup(backupJson) }
                                                status = when (result) {
                                                    is BackupRestoreResult.Restored -> "${result.recordCount} Vorgänge wurden wiederhergestellt."
                                                    is BackupRestoreResult.Rejected -> "Wiederherstellung fehlgeschlagen: ${result.reason}"
                                                }
                                                shipmentRevision++
                                                pendingBackupImport = null
                                                pendingBackupLegacy = false
                                            } finally {
                                                backupBusy = false
                                            }
                                        }
                                    }
                                },
                                onDismiss = {
                                    pendingBackupImport = null
                                    pendingBackupLegacy = false
                                }
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



