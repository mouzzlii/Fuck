package de.trakuna.app

internal fun normalizeTrackingNumber(raw: String): String =
    ShipmentRules.normalizeTrackingNumber(raw)


internal fun extractTrackingNumber(raw: String, rejectEan13: Boolean = false): String? =
    ShipmentRules.extractTrackingNumber(raw, MAX_SCAN_INPUT_CHARS, rejectEan13)

internal fun identifyCarrier(raw: String): String? =
    ShipmentRules.identifyCarrier(normalizeTrackingNumber(raw))

