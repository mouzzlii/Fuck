package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Form for the three locally recorded parcel-shop intake workflows. */
@Composable
internal fun ShipmentIntakeForm(
    number: String,
    recipient: String,
    note: String,
    status: String,
    storageIssue: String?,
    onRecipientChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCreate: (ShipmentFlow) -> Unit
) {
    Text("Neuen Vorgang mit der oben eingegebenen Sendungsnummer erfassen:")
    OutlinedTextField(
        value = recipient,
        onValueChange = { onRecipientChange(it.take(100)) },
        label = { Text("Empfänger / Kundenname (optional)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = note,
        onValueChange = { onNoteChange(it.take(500)) },
        label = { Text("Notiz (optional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
    if (storageIssue != null) {
        Text(
            "Erfassung derzeit gesperrt: $storageIssue",
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
    val enabled = storageIssue == null && normalizeTrackingNumber(number).isNotBlank()
    Button(onClick = { onCreate(ShipmentFlow.INCOMING_FOR_CUSTOMER) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text("Paket für Kunden annehmen")
    }
    Button(onClick = { onCreate(ShipmentFlow.OUTGOING_FROM_CUSTOMER) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text("Sendung vom Kunden annehmen")
    }
    Button(onClick = { onCreate(ShipmentFlow.CUSTOMER_RETURN) }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text("Retoure annehmen")
    }
    Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    HorizontalDivider()
}
