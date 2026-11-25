package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PartnerAppPickerDialog(
    carrier: String,
    candidates: List<PartnerCandidate>,
    showCarrierMenu: Boolean,
    showAllApps: Boolean,
    hasCurrentMapping: Boolean,
    onCarrierMenuChange: (Boolean) -> Unit,
    onCarrierSelected: (String) -> Unit,
    onAppSelected: (PartnerCandidate) -> Unit,
    onShowAllApps: () -> Unit,
    onRemoveMapping: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Betriebssoftware für $carrier zuordnen") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box {
                    OutlinedButton(onClick = { onCarrierMenuChange(true) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Paketdienst: $carrier")
                    }
                    DropdownMenu(
                        expanded = showCarrierMenu,
                        onDismissRequest = { onCarrierMenuChange(false) }
                    ) {
                        PartnerAppConfig.priorityCarriers.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { onCarrierSelected(option) }
                            )
                        }
                    }
                }
                Text("Wähle die installierte App, die zu diesem Paketdienst gehört. Die Zuordnung wird nur auf diesem Gerät gespeichert.")
                Text(if (candidates.isEmpty()) "Keine passende Paketdienst-App gefunden. Du kannst alle installierten Apps anzeigen." else "Installierte Apps:")
                candidates.take(30).forEach { app ->
                    TextButton(onClick = { onAppSelected(app) }, modifier = Modifier.fillMaxWidth()) {
                        Text("${app.label}  ·  ${app.packageName}")
                    }
                }
                if (!showAllApps) {
                    TextButton(onClick = onShowAllApps) { Text("Alle installierten Apps anzeigen") }
                }
                if (hasCurrentMapping) {
                    TextButton(onClick = onRemoveMapping, modifier = Modifier.fillMaxWidth()) {
                        Text("Zuordnung für $carrier entfernen")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}
