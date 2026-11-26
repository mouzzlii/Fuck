package de.trakuna.app

/**
 * DataWedge label types accepted by TRAKUNA.
 * Keep this allow-list aligned with the CameraX/ML Kit scanner formats.
 * Missing and unknown symbology metadata is rejected rather than guessed.
 */
internal const val DATAWEDGE_PACKAGE_NAME = "com.symbol.datawedge"

/**
 * Only Android 14 (API 34) and newer expose the original broadcast sender to a
 * BroadcastReceiver. On older versions an exported receiver cannot distinguish
 * a genuine DataWedge broadcast from an app forging the same action and extras.
 *
 * DataWedge Component Information signature verification protects delivery from
 * DataWedge to TRAKUNA; it does not authenticate broadcasts sent by other apps
 * to TRAKUNA. Therefore Zebra broadcast input is disabled below API 34.
 */
internal fun isDataWedgeSenderAccepted(apiLevel: Int, senderPackage: String?): Boolean =
    apiLevel >= 34 && senderPackage == DATAWEDGE_PACKAGE_NAME

/** User-facing explanation for a rejected DataWedge broadcast. */
internal fun dataWedgeRejectionMessage(apiLevel: Int): String =
    if (apiLevel < 34) {
        "Zebra-Scans über DataWedge sind auf dieser Android-Version aus Sicherheitsgründen deaktiviert. Bitte Kamera oder manuelle Eingabe verwenden."
    } else {
        "Zebra-Scan abgelehnt: Der Absender konnte nicht als DataWedge verifiziert werden."
    }

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

/** EAN-13 is a product identifier, not a parcel tracking number. */
internal fun isDataWedgeEan13(labelType: String?): Boolean =
    labelType?.trim()?.equals("LABEL-TYPE-EAN13", ignoreCase = true) == true
