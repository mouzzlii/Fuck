package de.trakuna.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
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

enum class ShipmentEventType { CREATED, STATUS_CHANGED, DETAILS_UPDATED, ARCHIVED, RESTORED }

data class ShipmentEvent(
    val at: Long,
    val type: ShipmentEventType,
    val fromStatus: ShipmentStatus? = null,
    val toStatus: ShipmentStatus? = null
)

data class ShipmentRecord(
    val id: String,
    val trackingNumber: String,
    val carrier: String,
    val flow: ShipmentFlow,
    val status: ShipmentStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val recipient: String = "",
    val note: String = "",
    val history: List<ShipmentEvent> = emptyList(),
    val archivedAt: Long? = null
)

sealed class ShipmentUpdate {
    data class Saved(val record: ShipmentRecord) : ShipmentUpdate()
    data class AlreadyExists(val record: ShipmentRecord) : ShipmentUpdate()
    data class Rejected(val reason: String) : ShipmentUpdate()
}

sealed class BackupValidation {
    data class Valid(val recordCount: Int) : BackupValidation()
    data class Invalid(val reason: String) : BackupValidation()
}

sealed class BackupRestoreResult {
    data class Restored(val recordCount: Int) : BackupRestoreResult()
    data class Rejected(val reason: String) : BackupRestoreResult()
}

/**
 * Local, bounded store. Updates are synchronously committed to SharedPreferences.
 * A production multi-device parcel-shop deployment will need a secured backend.
 */
class ShipmentStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    @Volatile private var initializationError: String? = null

    init {
        migrateLegacyPlaintext()
    }

    /** Migrate legacy plaintext only after the encrypted replacement is committed. */
    private fun migrateLegacyPlaintext() {
        runCatching {
            val encrypted = prefs.getString(KEY_RECORDS_ENCRYPTED, null)
            val legacy = prefs.getString(KEY_RECORDS, null)
            if (encrypted != null) {
                validateStoredPayload(decrypt(encrypted)) // Verify encryption AND record structure before removing any old copy.
                if (legacy != null && !prefs.edit().remove(KEY_RECORDS).commit()) {
                    error("Alte unverschlüsselte Daten konnten nicht entfernt werden.")
                }
            } else if (legacy != null) {
                val replacement = encrypt(legacy)
                if (!prefs.edit()
                        .putString(KEY_RECORDS_ENCRYPTED, replacement)
                        .remove(KEY_RECORDS)
                        .commit()
                ) {
                    error("Verschlüsselte Datenmigration konnte nicht dauerhaft gespeichert werden.")
                }
            }
        }.onFailure {
            initializationError = "Geschützte lokale Speicherung konnte nicht initialisiert werden: " +
                (it.message ?: "unbekannter Fehler")
        }
    }

    private fun validateStoredPayload(raw: String) {
        val array = JSONArray(raw)
        require(array.length() <= MAX_RECORDS) { "Lokaler Datenbestand überschreitet das zulässige Limit." }
        val records = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: error("Datensatz ${index + 1} ist beschädigt.")
                val record = decode(item) ?: error("Datensatz ${index + 1} ist unvollständig.")
                require(ShipmentRules.isValidRecord(record)) { "Datensatz ${index + 1} enthält ungültige Daten." }
                add(record)
            }
        }
        require(records.map { it.id }.distinct().size == records.size) { "Lokaler Datenbestand enthält doppelte Vorgangs-IDs." }
        require(ShipmentRules.hasNoDuplicateActiveShipments(records)) { "Lokaler Datenbestand enthält doppelte aktive Sendungen." }
    }

    private fun encryptionKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private fun encrypt(plainText: String): String {
        val plainBytes = plainText.toByteArray(Charsets.UTF_8)
        require(plainBytes.size <= MAX_STORAGE_BYTES) { "Lokaler Datenbestand überschreitet die sichere Speichergrenze." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val encrypted = cipher.doFinal(plainBytes)
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        require(encoded.length <= MAX_ENCRYPTED_STORAGE_CHARS) { "Verschlüsselte lokale Daten überschreiten die zulässige Größe." }
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > GCM_IV_BYTES) { "Verschlüsselte lokale Daten sind beschädigt." }
        require(payload.size <= MAX_STORAGE_BYTES + GCM_IV_BYTES + GCM_TAG_BYTES) {
            "Verschlüsselte lokale Daten überschreiten die zulässige Größe."
        }
        val iv = payload.copyOfRange(0, GCM_IV_BYTES)
        val encrypted = payload.copyOfRange(GCM_IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), javax.crypto.spec.GCMParameterSpec(128, iv))
        return decodeStrictUtf8(cipher.doFinal(encrypted))
    }

    @Synchronized
    fun all(): List<ShipmentRecord> = readRecords().getOrDefault(emptyList())

    @Synchronized
    fun storageError(): String? = readRecords().exceptionOrNull()?.message

    private fun readRecords(): Result<List<ShipmentRecord>> = runCatching {
        initializationError?.let { error(it) }
        val stored = prefs.getString(KEY_RECORDS_ENCRYPTED, null) ?: return@runCatching emptyList()
        val raw = decrypt(stored)
        val array = JSONArray(raw)
        val records = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: error("Datensatz ${index + 1} ist beschädigt.")
                val record = decode(item) ?: error("Datensatz ${index + 1} ist unvollständig.")
                require(ShipmentRules.isValidRecord(record)) { "Datensatz ${index + 1} enthält ungültige Daten." }
                add(record)
            }
        }
        require(records.size <= MAX_RECORDS) { "Lokaler Datenspeicher überschreitet das zulässige Limit." }
        require(records.map { it.id }.distinct().size == records.size) { "Lokaler Datenspeicher enthält doppelte Vorgangs-IDs." }
        require(ShipmentRules.hasNoDuplicateActiveShipments(records)) { "Lokaler Datenspeicher enthält doppelte aktive Sendungen." }
        records.sortedByDescending { it.updatedAt }
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

        val records = readRecords().getOrElse {
            return ShipmentUpdate.Rejected("Lokaler Datenspeicher kann nicht sicher gelesen werden. Es wurden keine Daten geändert. Bitte zuerst Support kontaktieren.")
        }.toMutableList()
        // A tracking number represents one physical parcel. Do not allow the same
        // active parcel to be registered in a second workflow by mistake.
        val existing = ShipmentRules.findActiveDuplicate(records, number)
        if (existing != null) return ShipmentUpdate.AlreadyExists(existing)
        if (records.size >= MAX_RECORDS) {
            return ShipmentUpdate.Rejected(
                "Speicherlimit von $MAX_RECORDS Vorgängen erreicht. Bitte zuerst eine CSV- oder JSON-Sicherung erstellen und nur archivierte Vorgänge nach Prüfung der Aufbewahrungsfristen endgültig löschen."
            )
        }

        val now = System.currentTimeMillis()
        val initialStatus = ShipmentRules.initialStatus(flow)
        val record = ShipmentRecord(
            id = UUID.randomUUID().toString(),
            trackingNumber = number,
            carrier = ShipmentRules.normalizeCarrierLabel(carrier.orEmpty()),
            flow = flow,
            status = initialStatus,
            createdAt = now,
            updatedAt = now,
            recipient = recipient.trim().take(100),
            note = note.trim().take(500),
            history = listOf(ShipmentEvent(now, ShipmentEventType.CREATED, toStatus = initialStatus))
        )
        records.add(record)
        if (!persist(records)) return ShipmentUpdate.Rejected("Speichern fehlgeschlagen. Bitte freien Gerätespeicher prüfen und den Vorgang erneut kontrollieren.")
        return ShipmentUpdate.Saved(record)
    }

    @Synchronized
    fun advance(id: String): ShipmentUpdate {
        val records = readRecords().getOrElse {
            return ShipmentUpdate.Rejected("Lokaler Datenspeicher kann nicht sicher gelesen werden. Es wurden keine Daten geändert.")
        }.toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index < 0) return ShipmentUpdate.Rejected("Sendung wurde nicht gefunden.")
        val current = records[index]
        if (current.archivedAt != null) return ShipmentUpdate.Rejected("Archivierte Vorgänge können nicht fortgeschrieben werden. Bitte zuerst wiederherstellen.")
        val next = ShipmentRules.nextStatus(current.flow, current.status)
            ?: return ShipmentUpdate.Rejected("Dieser Vorgang ist bereits abgeschlossen.")
        val changedAt = ShipmentRules.nextUpdateTimestamp(current.updatedAt, System.currentTimeMillis())
        val updated = current.copy(
            status = next,
            updatedAt = changedAt,
            history = (current.history + ShipmentEvent(changedAt, ShipmentEventType.STATUS_CHANGED, current.status, next)).takeLast(MAX_HISTORY_EVENTS)
        )
        records[index] = updated
        if (!persist(records)) return ShipmentUpdate.Rejected("Speichern fehlgeschlagen. Bitte den Vorgang erneut öffnen und prüfen.")
        return ShipmentUpdate.Saved(updated)
    }

    @Synchronized
    fun updateDetails(id: String, recipient: String, note: String): ShipmentUpdate {
        val records = readRecords().getOrElse {
            return ShipmentUpdate.Rejected("Lokaler Datenspeicher kann nicht sicher gelesen werden. Es wurden keine Daten geändert.")
        }.toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index < 0) return ShipmentUpdate.Rejected("Sendung wurde nicht gefunden.")
        if (records[index].archivedAt != null) return ShipmentUpdate.Rejected("Archivierte Vorgänge können nicht bearbeitet werden. Bitte zuerst wiederherstellen.")
        val changedAt = ShipmentRules.nextUpdateTimestamp(records[index].updatedAt, System.currentTimeMillis())
        val updated = records[index].copy(
            recipient = recipient.trim().take(100),
            note = note.trim().take(500),
            updatedAt = changedAt,
            history = (records[index].history + ShipmentEvent(changedAt, ShipmentEventType.DETAILS_UPDATED)).takeLast(MAX_HISTORY_EVENTS)
        )
        records[index] = updated
        if (!persist(records)) return ShipmentUpdate.Rejected("Speichern fehlgeschlagen. Bitte den Vorgang erneut öffnen und prüfen.")
        return ShipmentUpdate.Saved(updated)
    }

    @Synchronized
    fun archive(id: String): Boolean = changeArchiveState(id, archive = true)

    @Synchronized
    fun restoreArchived(id: String): Boolean = changeArchiveState(id, archive = false)

    private fun changeArchiveState(id: String, archive: Boolean): Boolean {
        val records = readRecords().getOrElse { return false }.toMutableList()
        val index = records.indexOfFirst { it.id == id }
        if (index < 0) return false
        val current = records[index]
        if ((current.archivedAt != null) == archive) return false
        if (archive && !ShipmentRules.isTerminalStatus(current.status)) return false
        if (!archive && ShipmentRules.findActiveDuplicate(records, current.trackingNumber) != null) return false
        val changedAt = ShipmentRules.nextUpdateTimestamp(current.updatedAt, System.currentTimeMillis())
        val updated = current.copy(
            updatedAt = changedAt,
            archivedAt = if (archive) changedAt else null,
            history = (current.history + ShipmentEvent(
                changedAt,
                if (archive) ShipmentEventType.ARCHIVED else ShipmentEventType.RESTORED
            )).takeLast(MAX_HISTORY_EVENTS)
        )
        if (!ShipmentRules.isValidRecord(updated)) return false
        records[index] = updated
        return persist(records)
    }

    @Synchronized
    fun remove(id: String): Boolean {
        val records = readRecords().getOrElse { return false }.toMutableList()
        val index = records.indexOfFirst { it.id == id }
        // Enforce archive-only deletion in the storage layer as well as in the UI.
        // This protects active parcel records if a future screen or caller invokes remove().
        if (index < 0 || !ShipmentRules.canPermanentlyDelete(records[index])) return false
        records.removeAt(index)
        return persist(records)
    }

    @Synchronized
    fun exportBackupJson(): String {
        val records = readRecords().getOrElse { error("Lokaler Datenspeicher ist beschädigt; Sicherung wurde nicht erstellt.") }
        return JSONObject().apply {
            put("format", BACKUP_FORMAT)
            put("schemaVersion", BACKUP_SCHEMA_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("recordCount", records.size)
            put("records", JSONArray().apply {
                records.forEach { record ->
                    put(JSONObject().apply {
                        put("id", record.id)
                        put("trackingNumber", record.trackingNumber)
                        put("carrier", record.carrier)
                        put("flow", record.flow.name)
                        put("status", record.status.name)
                        put("createdAt", record.createdAt)
                        put("updatedAt", record.updatedAt)
                        put("recipient", record.recipient)
                        put("note", record.note)
                        record.archivedAt?.let { put("archivedAt", it) }
                        put("history", encodeHistory(record.history))
                    })
                }
            })
        }.toString()
    }

    @Synchronized
    fun validateBackup(raw: String): BackupValidation {
        val parsed = runCatching {
            require(raw.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) { "Sicherungsdatei ist zu groß oder beschädigt." }
            val root = JSONObject(raw)
            require(root.optString("format") == BACKUP_FORMAT) { "Datei ist keine TRAKUNA-Sicherung." }
            require(root.optInt("schemaVersion", -1) in 1..BACKUP_SCHEMA_VERSION) { "Sicherungsversion wird nicht unterstützt." }
            require(root.optLong("exportedAt", -1L) > 0L) { "Sicherungszeitpunkt fehlt oder ist ungültig." }
            val array = root.optJSONArray("records") ?: error("Sicherungsdatei enthält keine Vorgangsliste.")
            require(root.optInt("recordCount", -1) == array.length()) { "Anzahl der Einträge stimmt nicht mit dem Sicherungskopf überein." }
            require(array.length() <= MAX_RECORDS) { "Sicherung enthält mehr als $MAX_RECORDS Vorgänge." }
            val records = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: error("Eintrag ${index + 1} ist beschädigt.")
                    val record = decode(item) ?: error("Eintrag ${index + 1} ist unvollständig oder ungültig.")
                    require(ShipmentRules.isValidRecord(record)) { "Eintrag ${index + 1} enthält ungültige Daten." }
                    add(record)
                }
            }
            require(records.map { it.id }.distinct().size == records.size) { "Sicherung enthält doppelte Vorgangs-IDs." }
            require(ShipmentRules.hasNoDuplicateActiveShipments(records)) { "Sicherung enthält doppelte aktive Sendungen desselben Vorgangstyps." }
            records
        }
        return if (parsed.isSuccess) {
            BackupValidation.Valid(parsed.getOrThrow().size)
        } else {
            BackupValidation.Invalid(parsed.exceptionOrNull()?.message ?: "Sicherungsdatei konnte nicht gelesen werden.")
        }
    }

    @Synchronized
    fun restoreBackup(raw: String): BackupRestoreResult {
        // A validated backup is the recovery path when the local encrypted store is damaged.
        // The user must explicitly confirm replacement in the UI; failed encryption/commit
        // leaves the restore operation rejected rather than claiming recovery.
        val validation = validateBackup(raw)
        if (validation !is BackupValidation.Valid) {
            return BackupRestoreResult.Rejected((validation as BackupValidation.Invalid).reason)
        }
        val root = runCatching { JSONObject(raw) }.getOrElse {
            return BackupRestoreResult.Rejected("Sicherungsdatei konnte nicht gelesen werden.")
        }
        val array = root.optJSONArray("records") ?: return BackupRestoreResult.Rejected("Vorgangsliste fehlt.")
        val records = buildList {
            for (index in 0 until array.length()) {
                decode(array.optJSONObject(index) ?: return BackupRestoreResult.Rejected("Eintrag beschädigt."))?.let(::add)
            }
        }
        val encoded = encodeRecords(records)
        val encrypted = runCatching { encrypt(encoded) }.getOrElse {
            return BackupRestoreResult.Rejected("Verschlüsselung fehlgeschlagen. Vorhandene Daten wurden nicht geändert.")
        }
        // Verify the encrypted replacement before touching the current store. This also makes
        // backup restore a real recovery path after init detected a corrupt local payload.
        val verified = runCatching { validateStoredPayload(decrypt(encrypted)) }
        if (verified.isFailure) {
            return BackupRestoreResult.Rejected("Wiederhergestellte Daten konnten vor dem Speichern nicht geprüft werden. Vorhandene Daten wurden nicht geändert.")
        }
        val saved = prefs.edit().putString(KEY_RECORDS_ENCRYPTED, encrypted).remove(KEY_RECORDS).commit()
        if (!saved) {
            return BackupRestoreResult.Rejected("Daten konnten nicht dauerhaft gespeichert werden. Vorhandene Daten wurden nicht absichtlich gelöscht.")
        }
        // init may have locked the store because the previous encrypted data was damaged.
        // Clear that lock only after a verified replacement has been durably committed.
        initializationError = null
        return BackupRestoreResult.Restored(records.size)
    }

    private fun encodeRecords(records: List<ShipmentRecord>): String = JSONArray().apply {
        records.sortedByDescending { it.updatedAt }.forEach { record ->
            put(JSONObject().apply {
                put("id", record.id)
                put("trackingNumber", record.trackingNumber)
                put("carrier", record.carrier)
                put("flow", record.flow.name)
                put("status", record.status.name)
                put("createdAt", record.createdAt)
                put("updatedAt", record.updatedAt)
                put("recipient", record.recipient)
                put("note", record.note)
                record.archivedAt?.let { put("archivedAt", it) }
                put("history", encodeHistory(record.history))
            })
        }
    }.toString()

    @Synchronized
    fun exportCsv(): String {
        // Include the locally stored event history in the export so a CSV backup
        // does not silently discard the chronology shown in the app. This remains
        // a device-local history, not a tamper-proof or centrally verified audit log.
        val header = listOf(
            "ID", "Sendungsnummer", "Paketdienst", "Vorgang", "Status",
            "Empfänger", "Notiz", "Erstellt (UTC)", "Aktualisiert (UTC)",
            "Archiviert (UTC)", "Vorgangshistorie (lokal)"
        )
        val rows = readRecords().getOrElse { error("Lokaler Datenspeicher ist beschädigt; CSV wurde nicht erstellt.") }.map { record ->
            val history = ShipmentRules.formatHistoryForExport(record.history)
            listOf(
                record.id,
                record.trackingNumber,
                record.carrier,
                record.flow.name,
                record.status.name,
                record.recipient,
                record.note,
                ShipmentRules.formatTimestampForExport(record.createdAt),
                ShipmentRules.formatTimestampForExport(record.updatedAt),
                record.archivedAt?.let(ShipmentRules::formatTimestampForExport).orEmpty(),
                history
            )
        }
        return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(";") { ShipmentRules.csvSafeCell(it) } }
    }

    private fun persist(records: List<ShipmentRecord>): Boolean =
        runCatching {
            initializationError?.let { error(it) }
            val encrypted = encrypt(encodeRecords(records))
            prefs.edit().putString(KEY_RECORDS_ENCRYPTED, encrypted).remove(KEY_RECORDS).commit()
        }.getOrDefault(false)

    private fun encodeHistory(history: List<ShipmentEvent>): JSONArray = JSONArray().apply {
        history.forEach { event ->
            put(JSONObject().apply {
                put("at", event.at)
                put("type", event.type.name)
                event.fromStatus?.let { put("fromStatus", it.name) }
                event.toStatus?.let { put("toStatus", it.name) }
            })
        }
    }

    private fun decodeHistory(record: JSONObject): List<ShipmentEvent> {
        // Only a missing field is a legacy backup without history. A present but
        // wrongly typed/null field is corruption and must not silently erase history.
        if (!record.has("history")) return emptyList()
        val array = record.optJSONArray("history")
            ?: error("Vorgangshistorie ist beschädigt oder hat ein ungültiges Format.")
        require(array.length() <= MAX_HISTORY_EVENTS) { "Vorgangshistorie enthält zu viele Einträge." }
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: error("Historieneintrag ${index + 1} ist beschädigt.")
                add(ShipmentEvent(
                    at = item.getLong("at"),
                    type = ShipmentEventType.valueOf(item.getString("type")),
                    fromStatus = item.optString("fromStatus").takeIf { it.isNotBlank() }?.let { ShipmentStatus.valueOf(it) },
                    toStatus = item.optString("toStatus").takeIf { it.isNotBlank() }?.let(ShipmentStatus::valueOf)
                ))
            }
        }
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
            note = item.optString("note"),
            history = decodeHistory(item),
            archivedAt = if (item.has("archivedAt")) item.getLong("archivedAt") else null
        )
    }.getOrNull()

    companion object {
        private const val BACKUP_FORMAT = "TRAKUNA_BACKUP"
        private const val BACKUP_SCHEMA_VERSION = 2
        private const val PREFS = "trakuna_shipments"
        // Legacy plaintext key used only for one-time migration.
        private const val KEY_RECORDS = "records_v1"
        private const val KEY_RECORDS_ENCRYPTED = "records_encrypted_v2"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "de.trakuna.app.shipments.aes.v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BYTES = 16
        private const val MAX_ENCRYPTED_STORAGE_CHARS = 2_800_000
        const val MAX_RECORDS = 500
        private const val MAX_HISTORY_EVENTS = 100
        private const val MAX_STORAGE_BYTES = 2 * 1024 * 1024
        const val MAX_BACKUP_BYTES = 4 * 1024 * 1024
        private val TERMINAL_STATUSES = setOf(
            ShipmentStatus.HANDED_TO_CUSTOMER,
            ShipmentStatus.HANDED_TO_CARRIER
        )
    }
}


/** Pure parcel workflow rules shared by the Android store and JVM unit tests. */
object ShipmentRules {
    /**
     * Parse a barcode or supported carrier QR URL without guessing arbitrary digits.
     * Kept in the pure rules layer so scanner formats can be covered by JVM tests.
     */
    fun extractTrackingNumber(raw: String, maxInputChars: Int = 4096): String? {
        if (raw.isEmpty() || raw.length > maxInputChars) return null

        // Zebra/DataWedge and GS1 scanners may prepend an AIM symbology identifier.
        // Strip only known, leading identifiers; never remove arbitrary punctuation.
        val cleaned = raw.trim()
            .replace(Regex("^(?:\\]C[0-9]|\\]d[0-9]|\\]Q[0-9])"), "")
            .trim()
        if (cleaned.isBlank()) return null

        fun plausible(value: String?): String? =
            value?.let(::normalizeTrackingNumber)?.takeIf(::isValidTrackingNumber)

        fun unique(values: List<String>): String? =
            values.distinctBy { it.uppercase() }.singleOrNull()

        val isHttpUrl = cleaned.startsWith("http://", true) || cleaned.startsWith("https://", true)
        val hasUrlScheme = Regex("^[A-Za-z][A-Za-z0-9+.-]*://").containsMatchIn(cleaned)
        // Never mine a tracking-number-looking substring from an unsupported URL scheme.
        if (hasUrlScheme && !isHttpUrl) return null

        if (isHttpUrl) {
            val uri = runCatching { java.net.URI(cleaned) }.getOrNull() ?: return null
            if (uri.scheme?.lowercase() !in setOf("http", "https") ||
                uri.host.isNullOrBlank() || uri.rawUserInfo != null
            ) return null

            val acceptedKeys = setOf(
                "trackingnumber", "tracking_number", "tracking", "trackid", "shipment",
                "shipmentid", "shipment_id", "consignment", "consignmentnumber",
                "parcel", "parcelnumber", "barcode", "piececode", "sendungsnummer", "snr", "id"
            )
            val queryCandidates = mutableListOf<String>()
            for (part in uri.rawQuery.orEmpty().split("&")) {
                if (part.isBlank()) continue
                val key = part.substringBefore("=", "")
                val value = part.substringAfter("=", "")
                val decodedKey = runCatching { java.net.URLDecoder.decode(key, "UTF-8") }.getOrNull()
                    ?: continue
                if (decodedKey.lowercase() !in acceptedKeys) continue
                val decodedValue = runCatching { java.net.URLDecoder.decode(value, "UTF-8") }.getOrNull()
                    ?: continue
                val candidate = plausible(decodedValue) ?: continue
                // Generic "id" is too ambiguous unless it has a known carrier structure.
                if (decodedKey.equals("id", ignoreCase = true) && identifyCarrier(candidate) == null) continue
                queryCandidates += candidate
            }
            if (queryCandidates.isNotEmpty()) return unique(queryCandidates)

            // A path segment is accepted only when it has a distinctive known format.
            // Arbitrary IDs in unrelated URLs are not treated as parcel tracking numbers.
            val pathCandidates = uri.path.orEmpty().split("/")
                .filter { it.isNotBlank() }
                .mapNotNull(::plausible)
                .filter { identifyCarrier(it) != null }
            return unique(pathCandidates)
        }

        val compact = plausible(cleaned)
        // If a known-format identifier is embedded inside a longer token, reject the
        // whole token rather than silently saving the surrounding prefix/suffix as a new ID.
        val embeddedKnown = Regex("(?i)(1Z[A-Z0-9]{16}|[A-Z]{2}[0-9]{9}AT|(?:JVGL|JJD)[A-Z0-9]{8,26})")
            .findAll(cleaned).map { it.value }.toList()
        if (compact != null && embeddedKnown.any { !it.equals(compact, ignoreCase = true) }) return null
        // A complete, compact scan can be retained as an unknown tracking number.
        // Whitespace-bearing text is only compacted when the result has a known structure.
        if (compact != null && (!cleaned.any(Char::isWhitespace) || identifyCarrier(compact) != null)) {
            return compact
        }

        // Extract only structurally distinctive formats from surrounding label text.
        // Word boundaries prevent matching a short valid-looking ID inside a longer token.
        val candidates = buildList {
            addAll(Regex("(?i)(?<![A-Z0-9])1Z[A-Z0-9]{16}(?![A-Z0-9])").findAll(cleaned).map { it.value }.toList())
            addAll(Regex("(?i)(?<![A-Z0-9])[A-Z]{2}[0-9]{9}AT(?![A-Z0-9])").findAll(cleaned).map { it.value }.toList())
            addAll(Regex("(?i)(?<![A-Z0-9])(?:JVGL|JJD)[A-Z0-9]{8,26}(?![A-Z0-9])").findAll(cleaned).map { it.value }.toList())
        }.mapNotNull(::plausible)
        return unique(candidates)
    }

    fun formatTimestamp(timestamp: Long, zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String =
        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", java.util.Locale.GERMANY)
            .withZone(zoneId)
            .format(java.time.Instant.ofEpochMilli(timestamp))

    fun formatTimestampForExport(timestamp: Long): String =
        java.time.Instant.ofEpochMilli(timestamp).toString()

    /** Stable, locale-independent representation of the device-local event history for CSV export. */
    fun formatHistoryForExport(history: List<ShipmentEvent>): String =
        history.joinToString(" | ") { event ->
            val transition = when {
                event.fromStatus != null && event.toStatus != null ->
                    " (${event.fromStatus.name} -> ${event.toStatus.name})"
                event.toStatus != null -> " (${event.toStatus.name})"
                else -> ""
            }
            "${formatTimestampForExport(event.at)} ${event.type.name}$transition"
        }

    /** Keep record chronology valid even if the device clock is moved backwards. */
    fun nextUpdateTimestamp(previousUpdatedAt: Long, now: Long): Long = maxOf(previousUpdatedAt, now)

    fun csvSafeCell(value: String): String {
        // Prefix spreadsheet formula starters so untrusted notes/names cannot execute as formulas.
        // Spreadsheet programs may ignore leading whitespace or a BOM when deciding
        // whether a cell starts with a formula. Inspect the first significant character,
        // but prefix the original cell so the exported value itself remains unchanged.
        val firstSignificant = value.dropWhile {
            it.isWhitespace() || it == '\uFEFF' || it == '\u200B'
        }.firstOrNull()
        val formulaPrefix = firstSignificant == '=' || firstSignificant == '+' || firstSignificant == '-' || firstSignificant == '@'
        val safeValue = if (formulaPrefix) "'$value" else value
        return "\"" + safeValue.replace("\"", "\"\"") + "\""
    }

    fun isValidRecord(record: ShipmentRecord): Boolean =
        record.id.isNotBlank() && record.id.length <= 80 &&
            isValidTrackingNumber(record.trackingNumber) &&
            record.carrier.length <= 80 && record.recipient.length <= 100 && record.note.length <= 500 &&
            record.createdAt > 0L && record.updatedAt >= record.createdAt &&
            record.history.size <= 100 &&
            (record.archivedAt == null || (record.archivedAt == record.updatedAt && isTerminalStatus(record.status))) &&
            record.history.zipWithNext().all { (a, b) -> b.at >= a.at } &&
            record.history.all { it.at in record.createdAt..record.updatedAt } &&
            isValidHistory(record) &&
            isStatusValidForFlow(record.flow, record.status)

    private fun isValidHistory(record: ShipmentRecord): Boolean {
        val history = record.history
        if (history.isEmpty()) return true

        val lastArchiveEvent = history.lastOrNull {
            it.type == ShipmentEventType.ARCHIVED || it.type == ShipmentEventType.RESTORED
        }
        if (record.archivedAt != null &&
            (lastArchiveEvent?.type != ShipmentEventType.ARCHIVED || lastArchiveEvent.at != record.archivedAt)
        ) return false
        if (record.archivedAt == null && lastArchiveEvent?.type == ShipmentEventType.ARCHIVED) return false

        val createdEvents = history.withIndex().filter { it.value.type == ShipmentEventType.CREATED }
        if (createdEvents.size > 1) return false
        if (createdEvents.isNotEmpty()) {
            val created = createdEvents.single()
            if (created.index != 0 || created.value.at != record.createdAt ||
                created.value.fromStatus != null ||
                created.value.toStatus != initialStatus(record.flow)
            ) return false
        }

        var knownStatus: ShipmentStatus? = null
        history.forEachIndexed { index, event ->
            when (event.type) {
                ShipmentEventType.CREATED -> {
                    if (index != 0 || knownStatus != null) return false
                    knownStatus = event.toStatus
                }
                ShipmentEventType.STATUS_CHANGED -> {
                    val from = event.fromStatus ?: return false
                    val to = event.toStatus ?: return false
                    if (!isStatusValidForFlow(record.flow, from) ||
                        !isStatusValidForFlow(record.flow, to) ||
                        nextStatus(record.flow, from) != to ||
                        (knownStatus != null && knownStatus != from)
                    ) return false
                    knownStatus = to
                }
                ShipmentEventType.DETAILS_UPDATED,
                ShipmentEventType.ARCHIVED,
                ShipmentEventType.RESTORED -> {
                    if (event.fromStatus != null || event.toStatus != null) return false
                }
            }
        }
        return knownStatus == null || knownStatus == record.status
    }

    fun normalizeTrackingNumber(raw: String): String =
        raw.trim().replace(Regex("\\s+"), "")

    fun normalizeCarrierLabel(raw: String): String {
        val trimmed = raw.trim().take(80)
        return when (trimmed.lowercase(java.util.Locale.ROOT).replace(Regex("\\s+"), " ")) {
            "hermes" -> "Hermes"
            "dpd" -> "DPD"
            "gls" -> "GLS"
            "dhl", "dhl paket" -> "DHL Paket"
            "ups" -> "UPS"
            "österreichische post", "österreichische post ag", "austrian post", "post at" -> "Österreichische Post"
            else -> trimmed
        }
    }

    fun resolveCarrierLabel(manual: String, trackingNumber: String): String =
        normalizeCarrierLabel(manual).ifBlank { identifyCarrier(trackingNumber).orEmpty() }

    fun isValidTrackingNumber(value: String): Boolean =
        value.length in 6..40 && value.any { it in '0'..'9' } &&
            value.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '-' }

    /**
     * Best-effort carrier hint only. These patterns are not authoritative and must
     * never be used to select a paid shipping product or confirm carrier acceptance.
     */
    fun identifyCarrier(raw: String): String? {
        val value = normalizeTrackingNumber(raw).uppercase()
        return when {
            Regex("^1Z[A-Z0-9]{16}$").matches(value) -> "UPS"
            Regex("^[A-Z]{2}[0-9]{9}AT$").matches(value) -> "Österreichische Post"
            Regex("^(?:JVGL|JJD)[A-Z0-9]{8,26}$").matches(value) -> "DHL Paket"
            else -> null
        }
    }

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
            ShipmentFlow.OUTGOING_FROM_CUSTOMER -> when (status) {
                ShipmentStatus.ACCEPTED_FROM_CUSTOMER -> ShipmentStatus.HANDED_TO_CARRIER
                else -> null
            }
            ShipmentFlow.CUSTOMER_RETURN -> when (status) {
                ShipmentStatus.RETURN_ACCEPTED -> ShipmentStatus.HANDED_TO_CARRIER
                else -> null
            }
        }

    /** Find an already active parcel regardless of workflow, preventing a second live record. */
    fun findActiveDuplicate(records: List<ShipmentRecord>, trackingNumber: String): ShipmentRecord? =
        records.firstOrNull {
            it.archivedAt == null &&
                it.trackingNumber.equals(trackingNumber, ignoreCase = true) &&
                !isTerminalStatus(it.status)
        }

    /**
     * Validate duplicates within each workflow for stored/legacy data compatibility.
     * New writes use findActiveDuplicate() across all workflows, but older backups
     * created before that rule may legitimately contain cross-workflow duplicates.
     */
    fun hasNoDuplicateActiveShipments(records: List<ShipmentRecord>): Boolean {
        val activeKeys = records
            .filter { it.archivedAt == null && !isTerminalStatus(it.status) }
            .map { it.trackingNumber.uppercase() to it.flow }
        return activeKeys.distinct().size == activeKeys.size
    }

    /** Permanent deletion is allowed only after explicit archival. */
    fun canPermanentlyDelete(record: ShipmentRecord): Boolean =
        record.archivedAt != null && isValidRecord(record)

    fun isTerminalStatus(status: ShipmentStatus): Boolean =
        status == ShipmentStatus.HANDED_TO_CUSTOMER || status == ShipmentStatus.HANDED_TO_CARRIER

    fun isStatusValidForFlow(flow: ShipmentFlow, status: ShipmentStatus): Boolean = when (flow) {
        ShipmentFlow.INCOMING_FOR_CUSTOMER -> status in setOf(
            ShipmentStatus.RECEIVED_BY_SHOP,
            ShipmentStatus.READY_FOR_PICKUP,
            ShipmentStatus.HANDED_TO_CUSTOMER
        )
        ShipmentFlow.OUTGOING_FROM_CUSTOMER -> status in setOf(
            ShipmentStatus.ACCEPTED_FROM_CUSTOMER,
            ShipmentStatus.HANDED_TO_CARRIER
        )
        ShipmentFlow.CUSTOMER_RETURN -> status in setOf(
            ShipmentStatus.RETURN_ACCEPTED,
            ShipmentStatus.HANDED_TO_CARRIER
        )
    }
}
