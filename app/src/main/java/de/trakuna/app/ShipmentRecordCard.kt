package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Reusable presentation for one local shipment record and its permitted actions. */
@Composable
internal fun ShipmentRecordCard(
    record: ShipmentRecord,
    onAdvance: () -> Unit,
    onHistory: () -> Unit,
    onArchive: () -> Unit,
    onEdit: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record.trackingNumber, style = MaterialTheme.typography.titleSmall)
            Text("${shipmentFlowLabel(record.flow)} · ${shipmentStatusLabel(record.status)}")
            if (record.archivedAt != null) {
                Text("Archiviert: ${ShipmentRules.formatTimestamp(record.archivedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Erfasst: ${ShipmentRules.formatTimestamp(record.createdAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Zuletzt geändert: ${ShipmentRules.formatTimestamp(record.updatedAt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (record.carrier.isNotBlank()) Text(record.carrier, style = MaterialTheme.typography.bodySmall)
            if (record.recipient.isNotBlank()) Text("Empfänger: ${record.recipient}", style = MaterialTheme.typography.bodySmall)
            if (record.note.isNotBlank()) Text("Notiz: ${record.note}", style = MaterialTheme.typography.bodySmall)
            val canAdvance = record.archivedAt == null && !ShipmentRules.isTerminalStatus(record.status)
            if (canAdvance) {
                TextButton(onClick = onAdvance) { Text(shipmentNextAction(record.flow, record.status)) }
            }
            TextButton(onClick = onHistory) { Text("Vorgangshistorie anzeigen (${record.history.size})") }
            if (record.archivedAt == null) {
                if (ShipmentRules.isTerminalStatus(record.status)) {
                    TextButton(onClick = onArchive) { Text("Vorgang archivieren") }
                }
                TextButton(onClick = onEdit) { Text("Details bearbeiten") }
            } else {
                TextButton(onClick = onRestore) { Text("Wiederherstellen") }
                TextButton(onClick = onDelete) { Text("Endgültig löschen …", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
