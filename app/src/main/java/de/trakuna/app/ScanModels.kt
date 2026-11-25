package de.trakuna.app

/** Maximum accepted raw scanner payload before parsing. */
internal const val MAX_SCAN_INPUT_CHARS = 4096

/**
 * Scan diagnostics are deliberately kept only in Compose memory.
 * Raw labels can contain personal or shipment data and must not be persisted or logged.
 */
internal data class ScanObservation(
    val source: String,
    val format: String,
    val rawValue: String,
    val displayValue: String?,
    val extractedValue: String?
)

internal data class ScanDiagnosticResult(
    val observations: List<ScanObservation>,
    val selectedValue: String?
)
