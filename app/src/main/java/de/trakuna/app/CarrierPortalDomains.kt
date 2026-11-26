package de.trakuna.app

import java.net.URI
import java.util.Locale

internal data class CarrierPortalProfile(
    val label: String,
    val allowedDomains: Set<String>
)

/**
 * One allowlist for carrier recognition and portal URL validation.
 * Number formats alone are only used where the existing rules are distinctive;
 * DPD/GLS/Hermes/FedEx/LogoiX numeric lengths are deliberately not guessed.
 */
internal object CarrierPortalDomains {
    val profiles: List<CarrierPortalProfile> = listOf(
        CarrierPortalProfile("DPD", setOf("dpd.com", "dpd.at")),
        CarrierPortalProfile("GLS", setOf("gls-group.eu", "gls-group.com", "gls-one.at")),
        CarrierPortalProfile("Hermes", setOf("hermesworld.com", "myhermes.de")),
        CarrierPortalProfile("UPS", setOf("ups.com")),
        CarrierPortalProfile("Österreichische Post", setOf("post.at")),
        CarrierPortalProfile("DHL Paket", setOf("dhl.de", "deutschepost.de")),
        CarrierPortalProfile("DHL Express", setOf("express.dhl")),
        CarrierPortalProfile("FedEx", setOf("fedex.com")),
        CarrierPortalProfile("LogoiX", setOf("logoix.com"))
    )

    fun canonicalCarrier(raw: String): String {
        val value = raw.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")
        profiles.firstOrNull { it.label.lowercase(Locale.ROOT) == value }?.let { return it.label }
        return when (value) {
            "austrian post", "post at", "österreichische post ag" -> "Österreichische Post"
            "dhl" -> "DHL Paket"
            else -> ""
        }
    }

    fun identifyFromUrl(raw: String): String? {
        if (raw.length > 4096) return null
        val uri = runCatching { URI(raw.trim()) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        if (scheme !in setOf("http", "https") || uri.rawUserInfo != null) return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        return profiles.firstOrNull { profile ->
            profile.allowedDomains.any { domain -> host == domain || host.endsWith(".$domain") }
        }?.label
    }

    fun validatePortalUrl(carrier: String, raw: String): String? {
        if (raw.length !in 1..2048) return null
        val canonical = canonicalCarrier(carrier)
        if (canonical.isBlank()) return null
        val profile = profiles.firstOrNull { it.label == canonical } ?: return null
        val uri = runCatching { URI(raw.trim()) }.getOrNull() ?: return null
        if (uri.isOpaque || uri.scheme?.lowercase(Locale.ROOT) != "https" || uri.rawUserInfo != null) return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        val allowed = profile.allowedDomains.any { domain -> host == domain || host.endsWith(".$domain") }
        if (!allowed) return null
        return uri.toASCIIString()
    }
}
