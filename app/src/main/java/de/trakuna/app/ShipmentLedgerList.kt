package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal data class ShipmentLedgerActions(
    val onToggleArchived: () -> Unit,
    val onSearchChange: (String) -> Unit,
    val onVisibleLimitChange: (Int) -> Unit,
    val onCsvExport: () -> Unit,
    val onBackupExport: () -> Unit,
    val onBackupImport: () -> Unit,
    val onAdvance: (ShipmentRecord) -> Unit,
    val onHistory: (ShipmentRecord) -> Unit,
    val onArchive: (ShipmentRecord) -> Unit,
    val onEdit: (ShipmentRecord) -> Unit,
    val onRestore: (ShipmentRecord) -> Unit,
    val onDelete: (ShipmentRecord) -> Unit
)

@Composable
internal fun ShipmentLedgerList(
    records: List<ShipmentRecord>,
    storageIssue: String?,
    backupBusy: Boolean,
    archived: Boolean,
    search: String,
    visibleRecordLimit: Int,
    actions: ShipmentLedgerActions
) {
    Text("Gespeicherte Vorgänge", style = MaterialTheme.typography.titleSmall)
    if (storageIssue == null) {
        Text(
            "${records.size} von ${ShipmentStore.MAX_RECORDS} möglichen Einträgen belegt · ${records.count { it.archivedAt == null }} aktiv · ${records.count { it.archivedAt != null }} archiviert",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Text(
            "ACHTUNG: Lokaler Datenspeicher kann nicht gelesen werden: $storageIssue. Neue Vorgänge und Exporte sind gesperrt. Du kannst eine vertrauenswürdige Sicherung wiederherstellen oder dich an den Support wenden.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
    OutlinedButton(onClick = actions.onToggleArchived, modifier = Modifier.fillMaxWidth()) {
        Text(if (archived) "Aktive Vorgänge anzeigen" else "Archivierte Vorgänge anzeigen")
    }
    Text(
        if (archived) "Archiv – Vorgänge bleiben erhalten und können wiederhergestellt werden."
        else "Aktive Vorgänge – abgeschlossene Vorgänge können archiviert werden.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = search,
        onValueChange = actions.onSearchChange,
        label = { Text("Sendungsnummer, Empfänger oder Paketdienst suchen") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedButton(onClick = actions.onCsvExport, enabled = storageIssue == null, modifier = Modifier.fillMaxWidth()) {
        Text("CSV-Datei exportieren")
    }
    OutlinedButton(onClick = actions.onBackupExport, enabled = storageIssue == null && !backupBusy, modifier = Modifier.fillMaxWidth()) {
        Text("Vollständige Datensicherung erstellen")
    }
    OutlinedButton(onClick = actions.onBackupImport, enabled = !backupBusy, modifier = Modifier.fillMaxWidth()) {
        Text("Datensicherung wiederherstellen")
    }
    if (storageIssue != null) {
        Text("Die gespeicherten Vorgänge werden nicht als leer angezeigt, weil der Speicher fehlerhaft gelesen wurde.", color = MaterialTheme.colorScheme.error)
    } else if (records.isEmpty()) {
        Text("Noch keine Paketshop-Vorgänge erfasst.")
    } else {
        val filteredRecords = records.filter { record ->
            (record.archivedAt != null) == archived &&
                (search.isBlank() || listOf(record.trackingNumber, record.recipient, record.carrier, shipmentStatusLabel(record.status)).any { it.contains(search, ignoreCase = true) })
        }
        if (filteredRecords.isEmpty()) Text("Keine passenden Sendungen gefunden.")
        val visibleRecords = filteredRecords.take(visibleRecordLimit)
        visibleRecords.forEach { record ->
            ShipmentRecordCard(
                record = record,
                onAdvance = { actions.onAdvance(record) },
                onHistory = { actions.onHistory(record) },
                onArchive = { actions.onArchive(record) },
                onEdit = { actions.onEdit(record) },
                onRestore = { actions.onRestore(record) },
                onDelete = { actions.onDelete(record) }
            )
        }
        if (filteredRecords.size > visibleRecords.size) {
            TextButton(
                onClick = { actions.onVisibleLimitChange((visibleRecordLimit + 100).coerceAtMost(filteredRecords.size)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Weitere ${filteredRecords.size - visibleRecords.size} Treffer anzeigen") }
        }
    }
}
