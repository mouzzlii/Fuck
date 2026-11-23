package de.trakuna.app

/**
 * Central configuration point for carrier parcel-shop/business apps.
 *
 * The four pilot carriers use separate app assignments on each Android device.
 * The receiving app may read the TRAKUNA tracking-number and carrier extras.
 *
 * If the receiving app does not implement this intent contract, TRAKUNA still
 * preserves the number in its local cache and copies it to the clipboard.
 */
object PartnerAppConfig {
    val priorityCarriers: List<String> = listOf("DPD", "GLS", "Hermes", "UPS")

    private val defaultPartnerPackage: String? = null

    private val carrierPackages: Map<String, String> = mapOf(
        // Add only package IDs confirmed by the carrier/operator for this shop.
    )

    fun packageFor(carrier: String?): String? =
        carrier?.let { carrierPackages[ShipmentRules.normalizeCarrierLabel(it)] } ?: defaultPartnerPackage

    /** Stable preference suffix for per-carrier app assignment. */
    fun preferenceSuffixForCarrier(carrier: String?): String? =
        when (carrier?.let { ShipmentRules.normalizeCarrierLabel(it) }) {
            "DPD" -> "DPD"
            "GLS" -> "GLS"
            "Hermes" -> "Hermes"
            "UPS" -> "UPS"
            else -> null
        }

    /** An explicit operator choice takes precedence over carrier defaults. */
    fun resolvePackage(carrier: String?, selectedPackage: String?): String? =
        selectedPackage?.trim()?.takeIf { it.isNotEmpty() } ?: packageFor(carrier)
}
