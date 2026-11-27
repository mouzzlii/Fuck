package de.trakuna.app

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
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
private const val DATAWEDGE_DATA_EXTRA = "com.symbol.datawedge.data_string"
private const val DATAWEDGE_LABEL_TYPE_EXTRA = "com.symbol.datawedge.label_type"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val handoffCache = remember(context) { SecureHandoffCache(context) }
            val initialHandoffRead = remember(handoffCache) { runCatching { handoffCache.hasPendingHandoff() } }
            var hasPendingHandoff by remember(handoffCache) { mutableStateOf(initialHandoffRead.getOrDefault(false)) }
            var showResumeHandoffConfirmation by remember { mutableStateOf(false) }
            var number by rememberSaveable { mutableStateOf("") }
            var showScanner by remember { mutableStateOf(false) }
            var scanDiagnostic by remember { mutableStateOf<ScanDiagnosticResult?>(null) }
            var showScanDiagnostics by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            var status by remember(handoffCache) {
                mutableStateOf(
                    if (initialHandoffRead.isFailure) "Gespeicherte Übergabe konnte nicht geprüft werden. Bitte erneut scannen oder manuell eingeben."
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
            var showPortalSettings by remember { mutableStateOf(false) }
            var portalSettingsCarrier by remember { mutableStateOf("DPD") }
            var openPortalAfterSave by remember { mutableStateOf(false) }
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
                        val cachedNumberRead = runCatching { handoffCache.hasPendingHandoff() }
                        hasPendingHandoff = cachedNumberRead.getOrDefault(false)
                        if (cachedNumberRead.isFailure) {
                            status = "Gespeicherte Übergabe konnte nicht geprüft werden. Bitte erneut scannen oder manuell eingeben."
                        }
                    }
                }
                this@MainActivity.lifecycle.addObserver(observer)
                onDispose { this@MainActivity.lifecycle.removeObserver(observer) }
            }
            // Keep one store instance for the screen: its synchronization and initialization state
            // must be shared by every read/write operation in this UI.
            val shipmentStore = remember(context) { ShipmentStore(context) }

            fun openCarrierPortal(carrier: String) {
                when (val result = CarrierPortalRouter.open(context, carrier)) {
                    is PortalOpenResult.Opened -> {
                        openPortalAfterSave = false
                        status = "Anbieter erkannt: ${result.carrier}. Das offizielle Portal wurde geöffnet. Die Sendungsnummer bleibt in TRAKUNA; es wurde nichts an den Anbieter übertragen oder gebucht."
                    }
                    is PortalOpenResult.Missing -> {
                        portalSettingsCarrier = result.carrier
                        openPortalAfterSave = true
                        showPortalSettings = true
                        status = "Anbieter erkannt: ${result.carrier}. Bitte einmalig die offizielle Portaladresse für diesen Paketshop hinterlegen."
                    }
                    is PortalOpenResult.Rejected -> {
                        openPortalAfterSave = false
                        status = result.message
                    }
                }
            }

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
                        showScanDiagnostics = false
                        if (!supportedFormat) {
                            status = "Zebra-Barcodeformat fehlt oder wird nicht unterstützt. Scan wurde aus Sicherheitsgründen nicht übernommen. Bitte DataWedge-Profil prüfen."
                            return
                        }
                        if (scanned.isNullOrBlank()) {
                            status = "Kein eindeutiger Wert erkannt. Details findest du unter Einstellungen → Scan-Diagnose."
                            return
                        }
                        number = scanned
                        val detectedCarrier = ShipmentRules.identifyCarrier(raw) ?: ShipmentRules.identifyCarrier(scanned)
                        if (detectedCarrier != null) {
                            carrierOverride = detectedCarrier
                            status = "Anbieter erkannt · $detectedCarrier"
                            openCarrierPortal(detectedCarrier)
                        } else {
                            status = "Sendungsnummer erkannt. Paketdienst bei Bedarf auswählen."
                        }
                    }
                }
                val filter = createDataWedgeIntentFilter()
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
                    backupBusy = true
                    status = "CSV-Datei wird gespeichert…"
                    backupScope.launch {
                        try {
                            val saved = withContext(Dispatchers.IO) {
                                runCatching {
                                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                                        it.write("\uFEFF")
                                        it.write(csv)
                                    } ?: error("Datei konnte nicht geöffnet werden.")
                                }
                            }
                            status = if (saved.isSuccess) "CSV-Datei wurde gespeichert."
                            else "CSV-Datei konnte nicht gespeichert werden: ${saved.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                        } finally {
                            backupBusy = false
                        }
                    }
                }
            }
            val shipmentRecords = remember(shipmentRevision) { shipmentStore.all() }
            val shipmentStorageIssue = remember(shipmentRevision) { shipmentStore.storageError() }

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .imePadding()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.w_signature),
                            contentDescription = "TRAKUNA",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(42.dp).fillMaxWidth()
                        )
                        Text("TRAKUNA", style = MaterialTheme.typography.headlineLarge, color = Forest)
                        if (TrakunaTestMode.SHADOW_MODE_ENABLED) {
                            Text("TESTMODUS", style = MaterialTheme.typography.labelMedium, color = Forest)
                        }

                        Button(
                            onClick = { showScanner = !showScanner },
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        ) {
                            Text(if (showScanner) "Scanner schließen" else "Barcode scannen")
                        }

                        if (hasPendingHandoff) {
                            OutlinedButton(
                                onClick = { showResumeHandoffConfirmation = true },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Letzte Sendung wiederaufnehmen") }
                        }

                        if (showScanner) {
                            ScannerPanel(
                                onDetected = { diagnostic ->
                                    showScanner = false
                                    scanDiagnostic = diagnostic
                                    showScanDiagnostics = false
                                    val scanned = diagnostic.selectedValue
                                    if (scanned == null) {
                                        status = "Kein eindeutiger Wert erkannt. Bitte erneut scannen oder Nummer manuell eingeben."
                                    } else {
                                        number = scanned
                                        val detectedCarrier = diagnostic.observations.asSequence()
                                            .mapNotNull { ShipmentRules.identifyCarrier(it.rawValue) }
                                            .firstOrNull() ?: ShipmentRules.identifyCarrier(scanned)
                                        if (detectedCarrier != null) {
                                            carrierOverride = detectedCarrier
                                            status = "Anbieter erkannt · $detectedCarrier"
                                            openCarrierPortal(detectedCarrier)
                                        } else {
                                            status = "Sendungsnummer erkannt. Paketdienst bei Bedarf auswählen."
                                        }
                                    }
                                },
                                onClose = { showScanner = false }
                            )
                        }

                        OutlinedTextField(
                            value = number,
                            onValueChange = { number = it.take(80) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Oder Sendungsnummer eingeben") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            )
                        )

                        Box(Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showCarrierMenu = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (carrierOverride.isBlank()) "Paketdienst auswählen (optional)" else "Paketdienst: $carrierOverride")
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

                        Button(
                            onClick = { showShipmentLedger = true },
                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) { Text("Sendung erfassen") }

                        OutlinedButton(
                            onClick = { showShipmentLedger = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Sendungen verwalten") }

                        Text(status, style = MaterialTheme.typography.bodyMedium, color = Forest)

                        TextButton(
                            onClick = { showSettings = !showSettings },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (showSettings) "Einstellungen schließen" else "Einstellungen") }

                        if (showSettings) {
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Einstellungen", style = MaterialTheme.typography.titleMedium, color = Forest)
                                    Text(if (wifiConnected) "WLAN verbunden" else "Kein WLAN verbunden", style = MaterialTheme.typography.bodySmall)
                                    OutlinedButton(
                                        onClick = { showScanDiagnostics = !showScanDiagnostics },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (showScanDiagnostics) "Scan-Diagnose ausblenden" else "Scan-Diagnose anzeigen")
                                    }
                                    if (showScanDiagnostics) {
                                        ScanDiagnosticsPanel(
                                            diagnostic = scanDiagnostic,
                                            expanded = true,
                                            onExpandedChange = { showScanDiagnostics = it },
                                            onClear = {
                                                scanDiagnostic = null
                                                showScanDiagnostics = false
                                            },
                                            onCopyResult = { copied ->
                                                status = if (copied) "Scan-Diagnose kopiert." else "Kopieren fehlgeschlagen."
                                            }
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val selectedCarrier = CarrierPortalDomains.canonicalCarrier(
                                                ShipmentRules.resolveCarrierLabel(carrierOverride, number)
                                            )
                                            portalSettingsCarrier = selectedCarrier.ifBlank { "DPD" }
                                            openPortalAfterSave = false
                                            showPortalSettings = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Anbieterportale konfigurieren") }
                                    OutlinedButton(
                                        onClick = {
                                            val selectedCarrier = ShipmentRules.resolveCarrierLabel(carrierOverride, number)
                                            if (selectedCarrier.isBlank()) {
                                                status = "Bitte zuerst einen Paketdienst auswählen."
                                            } else {
                                                openCarrierPortal(selectedCarrier)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Anbieterportal öffnen") }

                                    OutlinedButton(
                                        onClick = {
                                            val cleared = handoffCache.clear()
                                            hasPendingHandoff = !cleared && runCatching { handoffCache.hasPendingHandoff() }.getOrDefault(false)
                                            status = if (cleared) "Gespeicherte Übergabe wurde gelöscht."
                                            else "Gespeicherte Übergabe konnte nicht sicher gelöscht werden."
                                        },
                                        enabled = hasPendingHandoff,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Gespeicherte Übergabe löschen") }
                                    OutlinedButton(
                                        onClick = {
                                            val report = ShadowAuditLog.readAll(context)
                                            if (report.isBlank()) {
                                                status = "Noch keine protokollierten Testaktionen vorhanden."
                                            } else {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "TRAKUNA – Testprotokoll")
                                                    putExtra(Intent.EXTRA_TEXT, report)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Testprotokoll teilen"))
                                            }
                                        },
                                        enabled = ShadowAuditLog.readAll(context).isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Testprotokoll teilen") }
                                    if (TrakunaTestMode.SHADOW_MODE_ENABLED) {
                                        Text(
                                            "Übergaben werden in dieser Testversion nur simuliert. Echte Buchungen sind deaktiviert.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

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
                                                    val cachedHandoff = runCatching { handoffCache.trackingNumber() }.getOrNull()
                                                    if (cachedHandoff == result.record.trackingNumber) {
                                                        if (handoffCache.clear()) hasPendingHandoff = false
                                                        else status += " Die zugehörige gespeicherte Übergabe konnte nicht automatisch gelöscht werden."
                                                    }
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
                                                onCsvExport = { if (!backupBusy) confirmCsvExport = true },
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
                                    if (!backupBusy) {
                                        backupBusy = true
                                        status = "CSV-Export wird vorbereitet…"
                                        backupScope.launch {
                                            try {
                                                val exported = withContext(Dispatchers.IO) {
                                                    runCatching { shipmentStore.exportCsv() }
                                                }
                                                if (exported.isSuccess) {
                                                    pendingCsv = exported.getOrThrow()
                                                    backupBusy = false
                                                    val launched = runCatching {
                                                        csvDocumentLauncher.launch("TRAKUNA-Sendungen.csv")
                                                    }
                                                    if (launched.isFailure) {
                                                        pendingCsv = null
                                                        status = "CSV-Dateiauswahl konnte nicht geöffnet werden."
                                                    }
                                                } else {
                                                    status = exported.exceptionOrNull()?.message ?: "CSV-Export fehlgeschlagen."
                                                }
                                            } finally {
                                                backupBusy = false
                                            }
                                        }
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
                                    if (result is ShipmentUpdate.Saved && ShipmentRules.isTerminalStatus(result.record.status)) {
                                        val cachedHandoff = runCatching { handoffCache.trackingNumber() }.getOrNull()
                                        if (cachedHandoff == result.record.trackingNumber) {
                                            if (handoffCache.clear()) hasPendingHandoff = false
                                            else status += " Die zugehörige gespeicherte Übergabe konnte nicht automatisch gelöscht werden."
                                        }
                                    }
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
                        if (showResumeHandoffConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showResumeHandoffConfirmation = false },
                                title = { Text("Letzte Sendung wiederaufnehmen?") },
                                text = {
                                    Text("Eine zuvor übergebene Sendungsnummer wird nur innerhalb von 24 Stunden angeboten. Möchtest du sie erneut in das Eingabefeld übernehmen?")
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            val restored = runCatching { handoffCache.trackingNumber() }
                                            val restoredNumber = restored.getOrDefault("")
                                            if (restored.isSuccess && restoredNumber.isNotBlank()) {
                                                number = restoredNumber
                                                status = "Gespeicherte Sendungsnummer bewusst wiederaufgenommen."
                                            } else {
                                                status = if (restored.isFailure) "Gespeicherte Übergabe konnte nicht gelesen werden."
                                                else "Die gespeicherte Übergabe ist abgelaufen oder wurde bereits gelöscht."
                                            }
                                            hasPendingHandoff = runCatching { handoffCache.hasPendingHandoff() }.getOrDefault(false)
                                            showResumeHandoffConfirmation = false
                                        }
                                    ) { Text("Wiederaufnehmen") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showResumeHandoffConfirmation = false }) { Text("Abbrechen") }
                                }
                            )
                        }

                        if (showPortalSettings) {
                            CarrierPortalSettingsDialog(
                                context = context,
                                initialCarrier = portalSettingsCarrier,
                                onDismiss = {
                                    showPortalSettings = false
                                    openPortalAfterSave = false
                                },
                                onSave = { carrier, url ->
                                    val saved = CarrierPortalRouter.savePortalUrl(context, carrier, url)
                                    if (!saved) {
                                        status = "Portaladresse abgelehnt. Bitte eine gültige HTTPS-Adresse auf der offiziellen Domain von $carrier verwenden."
                                        false
                                    } else {
                                        status = "Offizielle Portaladresse für $carrier gespeichert."
                                        if (openPortalAfterSave) {
                                            openPortalAfterSave = false
                                            when (val result = CarrierPortalRouter.open(context, carrier)) {
                                                is PortalOpenResult.Opened -> status = "Offizielles Portal von ${result.carrier} geöffnet. Die Sendungsnummer bleibt in TRAKUNA; es wurde nichts übertragen oder gebucht."
                                                is PortalOpenResult.Missing -> status = "Portaladresse fehlt weiterhin."
                                                is PortalOpenResult.Rejected -> status = result.message
                                            }
                                        }
                                        true
                                    }
                                }
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



