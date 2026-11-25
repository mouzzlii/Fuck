package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ShipmentHistoryDialog(record: ShipmentRecord, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
internal fun EditShipmentDialog(
    record: ShipmentRecord,
    recipient: String,
    note: String,
    onRecipientChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sendungsdetails bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(value = recipient, onValueChange = { onRecipientChange(it.take(100)) }, label = { Text("Empfänger / Kundenname") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = note, onValueChange = { onNoteChange(it.take(500)) }, label = { Text("Interne Notiz") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text("Speichern") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun AdvanceShipmentDialog(
    record: ShipmentRecord,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val nextStatus = ShipmentRules.nextStatus(record.flow, record.status)
    AlertDialog(
        onDismissRequest = onDismiss,
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
        confirmButton = { TextButton(onClick = onConfirm) { Text("Bestätigen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun DeleteShipmentDialog(
    record: ShipmentRecord,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vorgang endgültig löschen?") },
        text = {
            Text(
                "Achtung: ${record.trackingNumber} wird dauerhaft von diesem Gerät entfernt. " +
                    "Auch die lokal gespeicherte Vorgangshistorie geht dabei verloren. " +
                    "Diese Löschung wird nicht in einem manipulationssicheren Prüfprotokoll festgehalten und kann nicht rückgängig gemacht werden. " +
                    "Wenn du den Vorgang noch dokumentieren musst, brich ab und erstelle zuerst eine Datensicherung."
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Endgültig löschen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
