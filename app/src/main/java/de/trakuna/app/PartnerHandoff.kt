package de.trakuna.app

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PersistableBundle

internal const val CACHE_PREFS = "trakuna_handoff"

// The pilot build is deliberately locked to shadow mode. Disabling this constant
// requires a separate, reviewed release-readiness change and real-world approval.
internal object TrakunaTestMode {
    const val SHADOW_MODE_ENABLED: Boolean = true

    fun auditCarrier(carrier: String?): String =
        carrier?.takeIf { it in PartnerAppConfig.priorityCarriers } ?: "OTHER"

    fun blockedHandoffMessage(carrier: String?): String {
        val label = auditCarrier(carrier).takeIf { it != "OTHER" } ?: "Paketdienst"
        return "TESTMODUS: Die Übergabe an $label wurde nur simuliert. " +
            "Die Partner-App wurde NICHT geöffnet; es wurde keine Buchung, " +
            "Labelbestellung oder externe Übermittlung ausgelöst."
    }
}

internal object ShadowAuditLog {
    private const val DIRECTORY = "test-evidence"
    private const val FILE_NAME = "shadow-events.jsonl"

    fun readAll(context: Context): String = runCatching {
        val file = java.io.File(java.io.File(context.filesDir, DIRECTORY), FILE_NAME)
        if (file.isFile) file.readText(Charsets.UTF_8).take(1_000_000) else ""
    }.getOrDefault("")

    @Synchronized
    fun recordPartnerHandoff(context: Context, carrier: String?): Boolean = runCatching {
        val directory = java.io.File(context.filesDir, DIRECTORY)
        check(directory.isDirectory || directory.mkdirs()) { "Testprotokoll-Ordner konnte nicht angelegt werden." }
        val record = org.json.JSONObject()
            .put("timestamp", java.time.Instant.now().toString())
            .put("event", "PARTNER_HANDOFF_BLOCKED")
            .put("mode", "SHADOW")
            .put("carrier", TrakunaTestMode.auditCarrier(carrier))
            .put("result", "BLOCKED_BEFORE_EXTERNAL_APP")
            .put("appVersion", BuildConfig.VERSION_NAME)
        val logFile = java.io.File(directory, FILE_NAME)
        check(!logFile.exists() || logFile.length() < 1_000_000L) { "Testprotokoll hat die maximale Größe erreicht." }
        java.io.FileOutputStream(logFile, true).bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.append(record.toString())
            writer.newLine()
            writer.flush()
        }
    }.isSuccess
}

data class HandoffResult(val message: String)

internal data class PartnerCandidate(
    val label: String,
    val packageName: String,
    val looksLikeCarrierApp: Boolean
)

internal fun findLaunchableApps(context: Context): List<PartnerCandidate> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0)
        .mapNotNull { info ->
            val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
            if (packageName == context.packageName) return@mapNotNull null
            val label = info.loadLabel(context.packageManager)?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapNotNull null
            val searchable = "$label $packageName".lowercase()
            val keywords = listOf("dhl", "dpd", "gls", "ups", "post", "paket", "parcel", "shipping", "versand", "zebra", "mde", "courier", "express")
            PartnerCandidate(label, packageName, keywords.any { searchable.contains(it) })
        }
        .distinctBy { it.packageName }
        .sortedWith(compareByDescending<PartnerCandidate> { it.looksLikeCarrierApp }.thenBy { it.label.lowercase() })
}


internal fun handoffToPartnerApp(context: Context, rawNumber: String, carrier: String?): HandoffResult {
    val number = normalizeTrackingNumber(rawNumber)
    if (!ShipmentRules.isValidTrackingNumber(number)) {
        return HandoffResult("Bitte eine gültige Sendungsnummer eingeben oder erneut scannen.")
    }

    // Persist the handoff before leaving TRAKUNA. Do not report success if Android
    // cannot confirm that the local cache was written.
    val preferences = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
    val handoffCache = SecureHandoffCache(context)
    val cached = handoffCache.save(number, carrier)
    if (!cached) {
        val detail = handoffCache.failureReason()
        return HandoffResult(
            "Sendungsnummer konnte nicht sicher gespeichert werden. " +
                (detail ?: "Bitte Gerätespeicher prüfen und erneut versuchen.")
        )
    }

    // Shadow mode is a hard gate: do not copy to the clipboard and do not
    // launch any external carrier app. The attempt is recorded without shipment data.
    if (TrakunaTestMode.SHADOW_MODE_ENABLED) {
        val logged = ShadowAuditLog.recordPartnerHandoff(context, carrier)
        val message = TrakunaTestMode.blockedHandoffMessage(carrier)
        return HandoffResult(
            if (logged) message else "$message Das lokale Testprotokoll konnte allerdings nicht geschrieben werden."
        )
    }

    // Clipboard is only a fallback. Mark it sensitive on Android 13+ and never let
    // a clipboard service failure crash the parcel-shop workflow.
    val copied = runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: error("Zwischenablage ist nicht verfügbar.")
        val clip = ClipData.newPlainText("TRAKUNA Sendungsnummer", number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)
    }.isSuccess
    val clipboardMessage = if (copied) "Sendungsnummer zusätzlich kopiert." else "Zwischenablage nicht verfügbar; Nummer bleibt im TRAKUNA-Cache."

    val carrierSuffix = PartnerAppConfig.preferenceSuffixForCarrier(carrier)
    val hasCarrierSpecificMappings = PartnerAppConfig.priorityCarriers.any {
        preferences.contains("partner_package_$it")
    }
    val carrierSpecificPackage = carrierSuffix?.let { preferences.getString("partner_package_$it", null) }
    val packageName = PartnerAppConfig.resolveSelectedPackage(
        carrier,
        carrierSpecificPackage,
        hasCarrierSpecificMappings,
        preferences.getString("partner_package", null)
    )
    if (packageName.isNullOrBlank()) {
        val carrierLabel = carrier?.takeIf { it.isNotBlank() } ?: "den Paketdienst"
        return HandoffResult(
            "Nummer $number ist im TRAKUNA-Cache gesichert. $clipboardMessage " +
                "Ordne zuerst eine eigene Betriebssoftware für $carrierLabel zu."
        )
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: return HandoffResult(
            "Nummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Die konfigurierte Partner-App ($packageName) ist nicht installiert oder Android kann sie nicht starten."
        )

    // Contract for an adapted partner app: read EXTRA_TRACKING_NUMBER from the launch intent.
    launchIntent.putExtra(EXTRA_TRACKING_NUMBER, number)
    launchIntent.putExtra(EXTRA_CARRIER, carrier.orEmpty())
    launchIntent.putExtra(Intent.EXTRA_TEXT, number)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    return try {
        context.startActivity(launchIntent)
        HandoffResult("Sendungsnummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Partner-App wird geöffnet. Automatische Übernahme nur bei unterstützten Intent-Extras oder einer passenden Schnittstelle.")
    } catch (_: Exception) {
        HandoffResult("Nummer ist im TRAKUNA-Cache gesichert. $clipboardMessage Die Partner-App ließ sich nicht öffnen.")
    }
}

const val EXTRA_TRACKING_NUMBER = "de.trakuna.app.extra.TRACKING_NUMBER"
const val EXTRA_CARRIER = "de.trakuna.app.extra.CARRIER"

/** Public accessor for a future in-process adapter or diagnostics screen. */
fun getCachedTrackingNumber(context: Context): String =
    SecureHandoffCache(context).trackingNumber()

