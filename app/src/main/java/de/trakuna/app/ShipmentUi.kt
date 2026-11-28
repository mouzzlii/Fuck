package de.trakuna.app

internal fun shipmentFlowLabel(flow: ShipmentFlow): String = when (flow) {
    ShipmentFlow.INCOMING_FOR_CUSTOMER -> "Eingang für Kunden"
    ShipmentFlow.OUTGOING_FROM_CUSTOMER -> "Annahme vom Kunden"
    ShipmentFlow.CUSTOMER_RETURN -> "Kundenretoure"
}

internal fun shipmentStatusLabel(status: ShipmentStatus): String = when (status) {
    ShipmentStatus.RECEIVED_BY_SHOP -> "Im Shop eingetroffen"
    ShipmentStatus.READY_FOR_PICKUP -> "Abholbereit"
    ShipmentStatus.HANDED_TO_CUSTOMER -> "An Kunden übergeben"
    ShipmentStatus.ACCEPTED_FROM_CUSTOMER -> "Vom Kunden angenommen"
    ShipmentStatus.RETURN_ACCEPTED -> "Retoure angenommen"
    ShipmentStatus.HANDED_TO_CARRIER -> "Lokal als an Paketdienst übergeben dokumentiert"
}

internal fun shipmentNextAction(flow: ShipmentFlow, status: ShipmentStatus): String = when {
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER && status == ShipmentStatus.RECEIVED_BY_SHOP -> "Als abholbereit markieren"
    flow == ShipmentFlow.INCOMING_FOR_CUSTOMER -> "An Kunden übergeben"
    flow == ShipmentFlow.CUSTOMER_RETURN -> "Retoure – Übergabe lokal dokumentieren"
    else -> "Übergabe an Paketdienst lokal dokumentieren"
}

internal fun shipmentResultText(result: ShipmentUpdate): String = when (result) {
    is ShipmentUpdate.Saved -> "Vorgang gespeichert: ${result.record.trackingNumber} · ${shipmentStatusLabel(result.record.status)}"
    is ShipmentUpdate.AlreadyExists -> "Diese Sendung ist bereits als ${shipmentFlowLabel(result.record.flow)} erfasst: ${result.record.trackingNumber} · ${shipmentStatusLabel(result.record.status)}"
    is ShipmentUpdate.Rejected -> result.reason
}

