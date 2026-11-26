package de.trakuna.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal sealed class PortalOpenResult {
    data class Opened(val carrier: String) : PortalOpenResult()
    data class Missing(val carrier: String) : PortalOpenResult()
    data class Rejected(val message: String) : PortalOpenResult()
}

/** Opens only an operator-configured HTTPS portal on the official carrier domain. */
internal object CarrierPortalRouter {
    private const val PREFS = "trakuna_carrier_portals"

    fun savedPortalUrl(context: Context, carrier: String): String? {
        val canonical = CarrierPortalDomains.canonicalCarrier(carrier)
        if (canonical.isBlank()) return null
        val key = preferenceKey(canonical)
        val stored = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)
            ?: return null
        return CarrierPortalDomains.validatePortalUrl(canonical, stored)
    }

    fun savePortalUrl(context: Context, carrier: String, rawUrl: String): Boolean {
        val canonical = CarrierPortalDomains.canonicalCarrier(carrier)
        val valid = CarrierPortalDomains.validatePortalUrl(canonical, rawUrl) ?: return false
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(preferenceKey(canonical), valid)
            .commit()
    }

    fun open(context: Context, carrier: String): PortalOpenResult {
        val canonical = CarrierPortalDomains.canonicalCarrier(carrier)
        if (canonical.isBlank()) return PortalOpenResult.Rejected("Bitte zuerst einen erkannten oder manuell ausgewählten Anbieter festlegen.")
        val url = savedPortalUrl(context, canonical) ?: return PortalOpenResult.Missing(canonical)
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            PortalOpenResult.Opened(canonical)
        } catch (_: ActivityNotFoundException) {
            PortalOpenResult.Rejected("Für das $canonical-Portal wurde kein geeigneter Browser gefunden.")
        } catch (_: Exception) {
            PortalOpenResult.Rejected("Das $canonical-Portal konnte nicht geöffnet werden.")
        }
    }

    private fun preferenceKey(carrier: String): String =
        "portal_" + carrier.lowercase(java.util.Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_")
}

@Composable
internal fun CarrierPortalSettingsDialog(
    context: Context,
    initialCarrier: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Boolean
) {
    var selectedCarrier by remember(initialCarrier) {
        mutableStateOf(CarrierPortalDomains.canonicalCarrier(initialCarrier).ifBlank { "DPD" })
    }
    var portalUrl by remember(selectedCarrier) {
        mutableStateOf(CarrierPortalRouter.savedPortalUrl(context, selectedCarrier).orEmpty())
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anbieterportale konfigurieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedCarrier)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        CarrierPortalDomains.profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.label) },
                                onClick = {
                                    selectedCarrier = profile.label
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = portalUrl,
                    onValueChange = { portalUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Offizielle Portaladresse (HTTPS)") },
                    placeholder = { Text("https://…") },
                    supportingText = {
                        Text("Nur offizielle Domains des gewählten Anbieters. Gespeichert wird ausschließlich die Portaladresse – niemals Benutzername, Passwort oder Sitzung.")
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (onSave(selectedCarrier, portalUrl)) onDismiss() }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
