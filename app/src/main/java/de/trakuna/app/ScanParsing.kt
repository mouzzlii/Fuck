package de.trakuna.app

internal fun normalizeTrackingNumber(raw: String): String =
    ShipmentRules.normalizeTrackingNumber(raw)


internal fun extractTrackingNumber(raw: String, rejectRetailProductBarcode: Boolean = false): String? =
    ShipmentRules.extractTrackingNumber(raw, MAX_SCAN_INPUT_CHARS, rejectRetailProductBarcode)

internal fun identifyCarrier(raw: String): String? =
    ShipmentRules.identifyCarrier(normalizeTrackingNumber(raw))

