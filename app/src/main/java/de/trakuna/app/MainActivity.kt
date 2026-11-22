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
            var showShipmentLedger by remember { mutableStateOf(false) }
            var shipmentRevision by remember { mutableStateOf(0) }
            var recipient by remember { mutableStateOf("") }
            var shipmentNote by remember { mutableStateOf("") }
            var shipmentSearch by remember { mutableStateOf("") }
            var pendingDelete by remember { mutableStateOf<ShipmentRecord?>(null) }
            var editingRecord by remember { mutableStateOf<ShipmentRecord?>(null) }
            var editRecipient by remember { mutableStateOf("") }
            var editNote by remember { mutableStateOf("") }
            val context = LocalContext.current
            var pendingCsv by remember { mutableStateOf<String?>(null) }
            var pendingBackupJson by remember { mutableStateOf<String?>(null) }
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
                        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                            ?: error("Datei konnte nicht gelesen werden.")
                    }
                    if (loaded.isFailure) {
                        status = "Datei konnte nicht gelesen werden: ${loaded.exceptionOrNull()?.message ?: "unbekannter Fehler"}"
                    } else {
                        val json = loaded.getOrThrow()
                        when (val validation = ShipmentStore(context).validateBackup(json)) {
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
            val shipmentRecords = remember(shipmentRevision) { ShipmentStore(context).all() }

            MaterialTheme(colorScheme = lightColorScheme(primary = Forest, background = Color(0xFFF3F6F3))) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFFF3F6F3)) {
                    Column(
                        Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
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
                            onClick = { showShipmentLedger = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Paketshop-Sendungen verwalten") }
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
                                        OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Empfänger / Kundenname (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = shipmentNote, onValueChange = { shipmentNote = it }, label = { Text("Notiz (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                                        Button(
                                            onClick = {
                                                val result = ShipmentStore(context).add(number, identifyCarrier(number), ShipmentFlow.INCOMING_FOR_CUSTOMER, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                shipmentRevision++
                                            },
                                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Paket für Kunden annehmen") }
                                        Button(
                                            onClick = {
                                                val result = ShipmentStore(context).add(number, identifyCarrier(number), ShipmentFlow.OUTGOING_FROM_CUSTOMER, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                shipmentRevision++
                                            },
                                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Sendung vom Kunden annehmen") }
                                        Button(
                                            onClick = {
                                                val result = ShipmentStore(context).add(number, identifyCarrier(number), ShipmentFlow.CUSTOMER_RETURN, recipient, shipmentNote)
                                                status = shipmentResultText(result)
                                                shipmentRevision++
                                            },
                                            enabled = normalizeTrackingNumber(number).isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) { Text("Retoure annehmen") }
                                        HorizontalDivider()
                                        Text("Gespeicherte Vorgänge", style = MaterialTheme.typography.titleSmall)
                                        Text("${shipmentRecords.size} von ${ShipmentStore.MAX_RECORDS} möglichen Einträgen belegt", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        ShipmentStore(context).storageError()?.let { storageIssue ->
                                            Text("ACHTUNG: Lokaler Datenspeicher kann nicht gelesen werden: $storageIssue. Es werden keine Änderungen oder Sicherungen vorgenommen.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                        }
                                        OutlinedTextField(value = shipmentSearch, onValueChange = { shipmentSearch = it }, label = { Text("Sendungsnummer, Empfänger oder Paketdienst suchen") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedButton(onClick = {
                                            val exported = runCatching { ShipmentStore(context).exportCsv() }
                                            if (exported.isSuccess) {
                                                pendingCsv = exported.getOrThrow()
                                                csvDocumentLauncher.launch("TRAKUNA-Sendungen.csv")
                                            } else {
                                                status = exported.exceptionOrNull()?.message ?: "CSV-Export fehlgeschlagen."
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) { Text("CSV-Datei exportieren") }
                                        OutlinedButton(onClick = {
                                            val exported = runCatching { ShipmentStore(context).exportBackupJson() }
                                            if (exported.isSuccess) {
                                                pendingBackupJson = exported.getOrThrow()
                                                backupDocumentLauncher.launch("TRAKUNA-Sicherung.json")
                                            } else {
                                                status = exported.exceptionOrNull()?.message ?: "Datensicherung fehlgeschlagen."
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) { Text("Vollständige Datensicherung erstellen") }
                                        OutlinedButton(onClick = {
                                            restoreDocumentLauncher.launch(arrayOf("application/json", "text/json", "application/octet-stream"))
                                        }, modifier = Modifier.fillMaxWidth()) { Text("Datensicherung wiederherstellen") }
                                        if (shipmentRecords.isEmpty()) {
                                            Text("Noch keine Paketshop-Vorgänge erfasst.")
                                        } else {
                                            val filteredRecords = shipmentRecords.filter { record ->
                                                shipmentSearch.isBlank() || listOf(record.trackingNumber, record.recipient, record.carrier, shipmentStatusLabel(record.status)).any { it.contains(shipmentSearch, ignoreCase = true) }
                                            }
                                            if (filteredRecords.isEmpty()) {
                                                Text("Keine passenden Sendungen gefunden.")
                                            }
                                            filteredRecords.take(100).forEach { record ->
                                                Card(Modifier.fillMaxWidth()) {
                                                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
                                                        Text("${shipmentFlowLabel(record.flow)} · ${shipmentStatusLabel(record.status)}")
                                                        if (record.carrier.isNotBlank()) Text(record.carrier, style = MaterialTheme.typography.bodySmall)
                                                        if (record.recipient.isNotBlank()) Text("Empfänger: ${record.recipient}", style = MaterialTheme.typography.bodySmall)
                                                        if (record.note.isNotBlank()) Text("Notiz: ${record.note}", style = MaterialTheme.typography.bodySmall)
                                                        val canAdvance = record.status != ShipmentStatus.HANDED_TO_CUSTOMER &&
                                                            record.status != ShipmentStatus.HANDED_TO_CARRIER
                                                        if (canAdvance) {
                                                            TextButton(onClick = {
                                                                val result = ShipmentStore(context).advance(record.id)
                                                                status = shipmentResultText(result)
                                                                shipmentRevision++
                                                            }) { Text(shipmentNextAction(record.flow, record.status)) }
                                                        }
                                                        TextButton(onClick = { editRecipient = record.recipient; editNote = record.note; editingRecord = record }) { Text("Details bearbeiten") }
                                                        TextButton(onClick = { pendingDelete = record }) { Text("Vorgang löschen", color = MaterialTheme.colorScheme.error) }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showShipmentLedger = false }) { Text("Schließen") }
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
                                        val result = ShipmentStore(context).restoreBackup(backupJson)
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
                                        val result = ShipmentStore(context).updateDetails(record.id, editRecipient, editNote)
                                        status = shipmentResultText(result)
                                        shipmentRevision++
                                        editingRecord = null
                                    }) { Text("Speichern") }
                                },
                                dismissButton = { TextButton(onClick = { editingRecord = null }) { Text("Abbrechen") } }
                            )
                        }
                        pendingDelete?.let { record ->
                            AlertDialog(
                                onDismissRequest = { pendingDelete = null },
                                title = { Text("Vorgang löschen?") },
                                text = { Text("Soll der Vorgang ${record.trackingNumber} endgültig von diesem Gerät gelöscht werden?") },
                                confirmButton = { TextButton(onClick = {
                                    val removed = ShipmentStore(context).remove(record.id)
                                    status = if (removed) "Vorgang wurde gelöscht." else "Vorgang konnte nicht gelöscht werden. Datenspeicher prüfen."
                                    shipmentRevision++
                                    pendingDelete = null
                                }) { Text("Löschen") } },
                                dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Abbrechen") } }
                            )
                        }
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
    ShipmentStatus.HANDED_TO_CARRIER -> "An Paketdienst übergeben"
}

private fun shipmentNextAction(flow: ShipmentFlow, status: ShipmentStatus): String = when {
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER && status == ShipmentStatus.RECEIVED_BY_SHOP -> "Als abholbereit markieren"
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER -> "An Kunden übergeben"
    flow == ShipmentFlow.CUSTOMER_RETURN -> "Retoure an Paketdienst übergeben"
    else -> "An Paketdienst übergeben"
}

private fun shipmentResultText(result: ShipmentUpdate): String = when (result) {
    is ShipmentUpdate.Saved -> "Vorgang gespeichert: ${result.record.trackingNumber} · ${shipmentStatusLabel(result.record.status)}"
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

    val preferences = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
    val packageName = PartnerAppConfig.packageFor(carrier) ?: preferences.getString("partner_package", null)
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
        HandoffResult("Sendungsnummer gesichert. Partner-App wird geöffnet. Automatische Übernahme nur bei unterstützten Intent-Extras oder einer passenden Schnittstelle.")
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
    // Some scanners prepend AIM symbology identifiers (for example ]C1 for Code 128).
    val cleaned = raw.trim()
        .removePrefix("]C1").removePrefix("]d2").removePrefix("]Q3")
        .trim()

    if (cleaned.startsWith("http://", true) || cleaned.startsWith("https://", true)) {
        val uri = runCatching { Uri.parse(cleaned) }.getOrNull()
        val acceptedKeys = setOf(
            "trackingnumber", "tracking_number", "tracking", "trackid", "shipment",
            "shipmentid", "shipment_id", "consignment", "consignmentnumber",
            "parcel", "parcelnumber", "barcode", "piececode", "sendungsnummer", "snr", "id"
        )
        // Query parameter names are not consistently cased across carrier QR formats.
        uri?.queryParameterNames?.firstOrNull { it.lowercase() in acceptedKeys }?.let { key ->
            uri.getQueryParameter(key)?.let { value ->
                normalizeTrackingNumber(value).takeIf { isPlausibleTrackingNumber(it) }?.let { return it }
            }
        }
        // Prefer the final path segment only when it looks like an actual shipment ID.
        uri?.pathSegments?.asReversed()?.firstOrNull { segment ->
            normalizeTrackingNumber(segment).let { isPlausibleTrackingNumber(it) && identifyCarrier(it) != null }
        }?.let { return normalizeTrackingNumber(it) }
        uri?.lastPathSegment?.let { segment ->
            normalizeTrackingNumber(segment).takeIf { isPlausibleTrackingNumber(it) }?.let { return it }
        }
        return null
    }

    val compact = normalizeTrackingNumber(cleaned)
    if (isPlausibleTrackingNumber(compact)) return compact

    // Labels sometimes encode a short human-readable prefix/suffix around the barcode.
    // Extract only well-known, structurally distinctive formats; do not guess arbitrary digits.
    val candidates = listOf(
        Regex("(?i)1Z[A-Z0-9]{16}").find(cleaned)?.value,
        Regex("(?i)[A-Z]{2}[0-9]{9}AT").find(cleaned)?.value,
        Regex("(?i)(?:JVGL|JJD)[A-Z0-9]{8,26}").find(cleaned)?.value
    )
    return candidates.filterNotNull().firstOrNull { isPlausibleTrackingNumber(it) }
}

private fun identifyCarrier(raw: String): String? {
    val value = normalizeTrackingNumber(raw).uppercase()
    return when {
        Regex("^1Z[A-Z0-9]{16}$").matches(value) -> "UPS"
        Regex("^[A-Z]{2}[0-9]{9}AT$").matches(value) -> "Österreichische Post"
        value.startsWith("JVGL") || value.startsWith("JJD") -> "DHL Paket"
        value.startsWith("JD") && value.length in 12..30 -> "DHL Paket"
        // Common national formats. Ambiguous lengths are deliberately left unassigned.
        value.matches(Regex("^[0-9]{13}$")) -> "Österreichische Post"
        value.matches(Regex("^[0-9]{14}$")) -> "DPD"
        value.matches(Regex("^[0-9]{11,12}$")) -> "GLS"
        value.matches(Regex("^[0-9]{10}$")) || value.matches(Regex("^[0-9]{20}$")) -> "DHL Paket"
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
                                    }.distinct()
                                    // Prefer a structurally recognized shipment number over unrelated QR/barcode data.
                                    val selected = values.sortedWith(
                                        compareByDescending<String> { identifyCarrier(it) != null }
                                            .thenByDescending { it.length in 10..22 }
                                            .thenByDescending { it.length }
                                    ).firstOrNull()
                                    selected?.let { value ->
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
            } catch (error: Exception) {
                cameraError = "Kamera konnte nicht gestartet werden. Bitte Kameraberechtigung prüfen oder Sendungsnummer manuell eingeben."
            }
        }, mainExecutor)
        onDispose {
            runCatching { provider?.unbindAll() }
            scanner.close()
            executor.shutdown()
        }
    }
}
