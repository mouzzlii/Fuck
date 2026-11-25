package de.trakuna.app

/**
 * DataWedge label types accepted by TRAKUNA.
 * Keep this allow-list aligned with the CameraX/ML Kit scanner formats.
 * Missing and unknown symbology metadata is rejected rather than guessed.
 */
internal fun isAllowedDataWedgeLabelType(labelType: String?): Boolean {
    val normalized = labelType?.trim()?.uppercase(java.util.Locale.ROOT) ?: return false
    return normalized in setOf(
        "LABEL-TYPE-QRCODE",
        "LABEL-TYPE-GS1-QRCODE",
        "LABEL-TYPE-CODE128",
        "LABEL-TYPE-EAN128",
        "LABEL-TYPE-CODE39",
        "LABEL-TYPE-CODE93",
        "LABEL-TYPE-CODABAR",
        "LABEL-TYPE-EAN13",
        "LABEL-TYPE-EAN8",
        "LABEL-TYPE-I2OF5",
        "LABEL-TYPE-UPCA",
        "LABEL-TYPE-UPCE0",
        "LABEL-TYPE-UPCE1"
    )
}
