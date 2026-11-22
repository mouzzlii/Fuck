package de.trakuna.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Offline-first shipment ledger for the parcel-shop workflows.
 * Records stay on this device; no cloud or carrier integration is implied.
 */
enum class ShipmentFlow {
    INCOMING_FOR_CUSTOMER,
    OUTGOING_FROM_CUSTOMER,
    CUSTOMER_RETURN
}

enum class ShipmentStatus {
    RECEIVED_BY_SHOP,
    READY_FOR_PICKUP,
    HANDED_TO_CUSTOMER,
    ACCEPTED_FROM_CUSTOMER,
    RETURN_ACCEPTED,
    HANDED_TO_CARRIER
}

data class ShipmentRecord(
    val id: String,
    val trackingNumber: String,
    val carrier: String,
    val flow: ShipmentFlow,
    val status: ShipmentStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val recipient: String = "",
    val note: String = ""
)

sealed class ShipmentUpdate {
    data class Saved(val record: ShipmentRecord) : ShipmentUpdate()
    data class Rejected(val reason: String) : ShipmentUpdate()
}

/**
 * Local, bounded store. Updates are serialized through SharedPreferences.apply().
 * A production multi-device parcel-shop deployment will need a secured backend.
 */
class ShipmentStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized
    fun all(): List<ShipmentRecord> {
        val raw = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    decode(item)?.let(::add)
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrDefault(emptyList())
    }

    @Synchronized
    fun add(
        rawTrackingNumber: String,
        carrier: String?,
        flow: ShipmentFlow,
        recipient: String = "",
        note: String = ""
    ): ShipmentUpdate {
        val number = ShipmentRules.normalizeTrackingNumber(rawTrackingNumber)
        if (!ShipmentRules.isValidTrackingNumber(number)) {
            return ShipmentUpdate.Rejected("Bitte eine gültige Sendungsnummer mit 6 bis 40 Zeichen eingeben.")
        }

        val records = all().toMutableList()
        val existing = records.firstOrNull {
            it.trackingNumber.equals(number, ignoreCase = true) &&
                it.flow == flow && it.status !in TERMINAL_STATUSES
        }
        if (existing != null) return ShipmentUpdate.Saved(existing)
        if (records.size >= MAX_RECORDS) {
            return ShipmentUpdate.Rejected(
                "Speicherlimit von $MAX_RECORDS Vorgängen erreicht. Bitte zuerst eine CSV-Sicherung exportieren und nicht mehr benötigte Einträge löschen."
            )
        }

        val now = System.currentTimeMillis()
        val initialStatus = ShipmentRules.initialStatus(flow)
        val record = ShipmentRecord(
            id = UUID.randomUUID().toString(),
            trackingNumber = number,
            carrier = carrier.orEmpty(),
            flow = flow,
            status = initialStatus,
            createdAt = now,
            updatedAt = now,
            recipient = recipient.trim().take(100),
            note = note.trim().take(500)
        )
        records.add(record)
        persist(records)
        return ShipmentUpdate.Saved(record)
    }

    @Synchronized
    fun advance(id: String): ShipmentUpdate {
        val records = all().toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index < 0) return ShipmentUpdate.Rejected("Sendung wurde nicht gefunden.")
        val current = records[index]
        val next = ShipmentRules.nextStatus(current.flow, current.status)
            ?: return ShipmentUpdate.Rejected("Dieser Vorgang ist bereits abgeschlossen.")
        val updated = current.copy(status = next, updatedAt = System.currentTimeMillis())
        records[index] = updated
        persist(records)
        return ShipmentUpdate.Saved(updated)
    }

    @Synchronized
    fun updateDetails(id: String, recipient: String, note: String): ShipmentUpdate {
        val records = all().toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index < 0) return ShipmentUpdate.Rejected("Sendung wurde nicht gefunden.")
        val updated = records[index].copy(
            recipient = recipient.trim().take(100),
            note = note.trim().take(500),
            updatedAt = System.currentTimeMillis()
        )
        records[index] = updated
        persist(records)
        return ShipmentUpdate.Saved(updated)
    }

    @Synchronized
    fun remove(id: String): Boolean {
        val records = all().toMutableList()
        val removed = records.removeAll { it.id == id }
        if (removed) persist(records)
        return removed
    }

    @Synchronized
    fun exportCsv(): String {
        fun cell(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
        val header = listOf("ID", "Sendungsnummer", "Paketdienst", "Vorgang", "Status", "Empfänger", "Notiz", "Erstellt", "Aktualisiert")
        val rows = all().map { record ->
            listOf(
                record.id,
                record.trackingNumber,
                record.carrier,
                record.flow.name,
                record.status.name,
                record.recipient,
                record.note,
                record.createdAt.toString(),
                record.updatedAt.toString()
            )
        }
        return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(";") { cell(it) } }
    }

    private fun persist(records: List<ShipmentRecord>) {
        val array = JSONArray()
        records.sortedByDescending { it.updatedAt }.forEach { record ->
            array.put(JSONObject().apply {
                put("id", record.id)
                put("trackingNumber", record.trackingNumber)
                put("carrier", record.carrier)
                put("flow", record.flow.name)
                put("status", record.status.name)
                put("createdAt", record.createdAt)
                put("updatedAt", record.updatedAt)
                put("recipient", record.recipient)
                put("note", record.note)
            })
        }
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply()
    }

    private fun decode(item: JSONObject): ShipmentRecord? = runCatching {
        ShipmentRecord(
            id = item.getString("id"),
            trackingNumber = item.getString("trackingNumber"),
            carrier = item.optString("carrier"),
            flow = ShipmentFlow.valueOf(item.getString("flow")),
            status = ShipmentStatus.valueOf(item.getString("status")),
            createdAt = item.getLong("createdAt"),
            updatedAt = item.getLong("updatedAt"),
            recipient = item.optString("recipient"),
            note = item.optString("note")
        )
    }.getOrNull()

    companion object {
        private const val PREFS = "trakuna_shipments"
        private const val KEY_RECORDS = "records_v1"
        const val MAX_RECORDS = 500
        private val TERMINAL_STATUSES = setOf(
            ShipmentStatus.HANDED_TO_CUSTOMER,
            ShipmentStatus.HANDED_TO_CARRIER
        )
    }
}


/** Pure parcel workflow rules shared by the Android store and JVM unit tests. */
object ShipmentRules {
    fun normalizeTrackingNumber(raw: String): String =
        raw.trim().replace(Regex("\\s+"), "")

    fun isValidTrackingNumber(value: String): Boolean =
        value.length in 6..40 && value.any(Char::isDigit) &&
            value.all { it.isLetterOrDigit() || it == '-' }

    fun initialStatus(flow: ShipmentFlow): ShipmentStatus = when (flow) {
        ShipmentFlow.INCOMING_FOR_CUSTOMER -> ShipmentStatus.RECEIVED_BY_SHOP
        ShipmentFlow.OUTGOING_FROM_CUSTOMER -> ShipmentStatus.ACCEPTED_FROM_CUSTOMER
        ShipmentFlow.CUSTOMER_RETURN -> ShipmentStatus.RETURN_ACCEPTED
    }

    fun nextStatus(flow: ShipmentFlow, status: ShipmentStatus): ShipmentStatus? =
        when (flow) {
            ShipmentFlow.INCOMING_FOR_CUSTOMER -> when (status) {
                ShipmentStatus.RECEIVED_BY_SHOP -> ShipmentStatus.READY_FOR_PICKUP
                ShipmentStatus.READY_FOR_PICKUP -> ShipmentStatus.HANDED_TO_CUSTOMER
                else -> null
            }
            ShipmentFlow.OUTGOING_FROM_CUSTOMER,
            ShipmentFlow.CUSTOMER_RETURN -> when (status) {
                ShipmentStatus.ACCEPTED_FROM_CUSTOMER,
                ShipmentStatus.RETURN_ACCEPTED -> ShipmentStatus.HANDED_TO_CARRIER
                else -> null
            }
        }
}
