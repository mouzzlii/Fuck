package de.trakuna.app

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun CsvExportConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CSV-Datei exportieren?") },
        text = {
            Text("Die CSV-Datei enthält Sendungsnummern, Empfängernamen, interne Notizen und die lokal gespeicherte Vorgangshistorie. Sie wird unverschlüsselt gespeichert. Die Historie ist kein manipulationssicherer Prüfbeleg. Wähle einen geschützten Speicherort und gib die Datei nur an berechtigte Personen weiter.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("CSV erstellen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun BackupExportConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Datensicherung erstellen?") },
        text = {
            Text("Die Sicherungsdatei enthält Sendungsnummern, Empfängernamen und interne Notizen. Sie wird mit einem eigenen Passwort verschlüsselt. Bewahre das Passwort getrennt von der Datei auf; ohne das Passwort ist die Sicherung nicht wiederherstellbar.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Sicherung erstellen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun BackupImportConfirmationDialog(
    recordCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Datensicherung wiederherstellen?") },
        text = {
            Text("Die Sicherung enthält $recordCount Vorgänge. Die aktuell auf diesem Gerät gespeicherten Vorgänge werden vollständig ersetzt. Erstelle bei Bedarf zuerst eine Sicherung des aktuellen Standes.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Ersetzen und wiederherstellen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
