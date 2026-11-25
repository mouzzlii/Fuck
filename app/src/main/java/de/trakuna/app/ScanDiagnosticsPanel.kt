package de.trakuna.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/** Displays transient scan observations without persisting or logging raw label content. */
@Composable
internal fun ScanDiagnosticsPanel(
    diagnostic: ScanDiagnosticResult?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClear: () -> Unit,
    onCopyResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Scan-Diagnose", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { onExpandedChange(!expanded) }) {
                        Text(if (expanded) "Ausblenden" else "Anzeigen")
                    }
                    if (diagnostic != null) {
                        TextButton(onClick = onClear) { Text("Löschen") }
                    }
                }
            }
            Text(
                "Nur temporär im Arbeitsspeicher. Rohdaten können Sendungs- oder persönliche Informationen enthalten; sie werden nicht gespeichert oder protokolliert.",
                style = MaterialTheme.typography.bodySmall
            )
            if (diagnostic == null) {
                Text(
                    "Noch kein Kamerascan zur Diagnose empfangen. Starte den Scanner und halte den GLS-QR-Code ruhig vor die Kamera. Sobald ML Kit einen Barcode erkennt, werden Rohinhalt und Auswertung hier angezeigt.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (expanded) {
                Text("Übernommener Wert: ${diagnostic.selectedValue ?: "Kein eindeutiger Wert"}", style = MaterialTheme.typography.bodyMedium)
                Button(
                    onClick = {
                        val report = buildString {
                            appendLine("TRAKUNA – Scan-Diagnose")
                            appendLine("Übernommener Wert: ${diagnostic.selectedValue ?: "Kein eindeutiger Wert"}")
                            diagnostic.observations.take(30).forEachIndexed { index, observation ->
                                appendLine()
                                appendLine("Code ${index + 1}")
                                appendLine("Quelle: ${observation.source}")
                                appendLine("Format: ${observation.format}")
                                appendLine("Auswertung: ${observation.extractedValue ?: "Kein Sendungsnummern-Kandidat"}")
                                appendLine("Rohinhalt:")
                                appendLine(observation.rawValue.take(MAX_SCAN_INPUT_CHARS))
                                observation.displayValue?.takeIf { it != observation.rawValue }?.let {
                                    appendLine("Anzeigeinhalt: $it")
                                }
                            }
                            if (diagnostic.observations.size > 30) {
                                appendLine()
                                appendLine("Hinweis: Weitere Codes wurden aus Platzgründen ausgelassen.")
                            }
                        }.take(100_000)
                        val copied = runCatching {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                ?: error("Zwischenablage ist nicht verfügbar.")
                            clipboard.setPrimaryClip(ClipData.newPlainText("TRAKUNA Scan-Diagnose", report))
                        }.isSuccess
                        onCopyResult(copied)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fehlerdiagnose kopieren")
                }
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
