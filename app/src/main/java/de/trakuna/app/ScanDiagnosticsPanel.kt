package de.trakuna.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Displays transient scan observations without persisting or logging raw label content. */
@Composable
internal fun ScanDiagnosticsPanel(
    diagnostic: ScanDiagnosticResult,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClear: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Scan-Diagnose", style = MaterialTheme.typography.titleMedium, color = Forest)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onExpandedChange(!expanded) }) {
                        Text(if (expanded) "Ausblenden" else "Anzeigen")
                    }
                    TextButton(onClick = onClear) { Text("Löschen") }
                }
            }
            Text(
                "Nur temporär im Arbeitsspeicher. Rohdaten können Sendungs- oder persönliche Informationen enthalten; sie werden nicht gespeichert oder protokolliert.",
                style = MaterialTheme.typography.bodySmall
            )
            if (expanded) {
                Text("Übernommener Wert: ${diagnostic.selectedValue ?: "Kein eindeutiger Wert"}", style = MaterialTheme.typography.bodyMedium)
                diagnostic.observations.forEachIndexed { index, observation ->
                    HorizontalDivider()
                    Text("Code ${index + 1} · ${observation.source} · ${observation.format}", style = MaterialTheme.typography.labelLarge)
                    Text("Auswertung: ${observation.extractedValue ?: "Kein Sendungsnummern-Kandidat"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Rohinhalt:", style = MaterialTheme.typography.labelMedium)
                    Text(observation.rawValue.take(MAX_SCAN_INPUT_CHARS), style = MaterialTheme.typography.bodySmall)
                    observation.displayValue?.takeIf { it != observation.rawValue }?.let {
                        Text("Anzeigeinhalt: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
