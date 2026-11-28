package de.trakuna.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions

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
internal fun BackupExportConfirmationDialog(
    password: String,
    passwordConfirm: String,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verschlüsselte Datensicherung") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Die Sicherung enthält Sendungsnummern, Empfängernamen und interne Notizen. Sie wird mit AES-256-GCM verschlüsselt. Verwende mindestens 12 Zeichen und bewahre das Passwort getrennt auf. Ohne dieses Passwort ist die Sicherung nicht wiederherstellbar.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { onPasswordChange(it.take(128)) },
                    label = { Text("Sicherungspasswort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { onPasswordConfirmChange(it.take(128)) },
                    label = { Text("Passwort wiederholen") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = password.length >= 12 && password == passwordConfirm,
                onClick = onConfirm
            ) { Text("Verschlüsselt sichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun EncryptedBackupImportPasswordDialog(
    password: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    busy: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sicherung entschlüsseln") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Diese Sicherung ist verschlüsselt. Gib das beim Erstellen verwendete Passwort ein.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { onPasswordChange(it.take(128)) },
                    label = { Text("Sicherungspasswort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = password.isNotEmpty() && !busy, onClick = onConfirm) {
                Text("Entschlüsseln")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
internal fun BackupImportConfirmationDialog(
    recordCount: Int,
    isLegacy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Datensicherung wiederherstellen?") },
        text = {
            Text((if (isLegacy) "Achtung: Diese ältere Sicherung ist unverschlüsselt. " else "Die verschlüsselte Sicherung wurde erfolgreich geprüft. ") + "Sie enthält $recordCount Vorgänge. Die aktuell auf diesem Gerät gespeicherten Vorgänge werden vollständig ersetzt. Erstelle bei Bedarf zuerst eine eigene Sicherung des aktuellen Standes.")
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Ersetzen und wiederherstellen") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
