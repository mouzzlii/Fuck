package de.trakuna.app

/**
 * Only edit this file when adapting TRAKUNA to a carrier's business app.
 *
 * Put the Android applicationId/package name of the installed parcel-shop app
 * into defaultPartnerPackage, or map individual carriers in carrierPackages.
 *
 * The receiving app can read:
 *   intent.getStringExtra("de.trakuna.app.extra.TRACKING_NUMBER")
 *   intent.getStringExtra("de.trakuna.app.extra.CARRIER")
 *
 * If the partner app does not support these intent extras, TRAKUNA still keeps
 * the number in its own local cache and copies it to the clipboard as fallback.
 */
object PartnerAppConfig {
    private const val defaultPartnerPackage: String? = null

    private val carrierPackages = mapOf(
        // "Österreichische Post" to "official.business.app.package",
        // "DHL Paket" to "official.business.app.package",
        // "DPD" to "official.business.app.package",
        // "GLS" to "official.business.app.package",
        // "UPS" to "official.business.app.package"
    )

    fun packageFor(carrier: String?): String? =
        carrier?.let { carrierPackages[it] } ?: defaultPartnerPackage
}
