package de.trakuna.app

/**
 * Central configuration point for carrier parcel-shop/business apps.
 *
 * Set defaultPartnerPackage to one installed app package ID, or add carrier
 * specific package IDs below. The receiving app may read:
 *   de.trakuna.app.extra.TRACKING_NUMBER
 *   de.trakuna.app.extra.CARRIER
 *
 * If the receiving app does not implement this intent contract, TRAKUNA still
 * preserves the number in its local cache and copies it to the clipboard.
 */
object PartnerAppConfig {
    private val defaultPartnerPackage: String? = null

    private val carrierPackages: Map<String, String> = mapOf(
        // "Österreichische Post" to "official.business.app.package",
        // "DHL Paket" to "official.business.app.package",
        // "DPD" to "official.business.app.package",
        // "GLS" to "official.business.app.package",
        // "UPS" to "official.business.app.package"
    )

    fun packageFor(carrier: String?): String? =
        carrier?.let { carrierPackages[it] } ?: defaultPartnerPackage
}
