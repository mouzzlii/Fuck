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
        require(plainBytes.size <= MAX_BACKUP_BYTES) { "Lokaler Datenbestand überschreitet die sichere Speichergrenze." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val encrypted = cipher.doFinal(plainBytes)
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        require(encoded.length <= MAX_ENCRYPTED_STORAGE_CHARS) { "Verschlüsselte lokale Daten überschreiten die zulässige Größe." }
        val payload = Base64.decode(encoded, Base64.NO_WRAP)
        require(payload.size > GCM_IV_BYTES) { "Verschlüsselte lokale Daten sind beschädigt." }
        require(payload.size <= MAX_BACKUP_BYTES + GCM_IV_BYTES + GCM_TAG_BYTES) {
            "Verschlüsselte lokale Daten überschreiten die zulässige Größe."
        }
        val iv = payload.copyOfRange(0, GCM_IV_BYTES)
        val encrypted = payload.copyOfRange(GCM_IV_BYTES, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), javax.crypto.spec.GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
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
        val existing = records.firstOrNull {
            it.trackingNumber.equals(number, ignoreCase = true) &&
                it.flow == flow && it.status !in TERMINAL_STATUSES
        }
        if (existing != null) return ShipmentUpdate.AlreadyExists(existing)
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
            carrier = ShipmentRules.normalizeCarrierLabel(carrier.orEmpty()),
            flow = flow,
            status = initialStatus,
            createdAt = now,
            updatedAt = now,
            recipient = recipient.trim().take(100),
            note = note.trim().take(500)
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
        val next = ShipmentRules.nextStatus(current.flow, current.status)
            ?: return ShipmentUpdate.Rejected("Dieser Vorgang ist bereits abgeschlossen.")
        val updated = current.copy(status = next, updatedAt = System.currentTimeMillis())
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
        val updated = records[index].copy(
            recipient = recipient.trim().take(100),
            note = note.trim().take(500),
            updatedAt = System.currentTimeMillis()
        )
        records[index] = updated
        if (!persist(records)) return ShipmentUpdate.Rejected("Speichern fehlgeschlagen. Bitte den Vorgang erneut öffnen und prüfen.")
        return ShipmentUpdate.Saved(updated)
    }

    @Synchronized
    fun remove(id: String): Boolean {
        val records = readRecords().getOrElse { return false }.toMutableList()
        val removed = records.removeAll { it.id == id }
        if (!removed) return false
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
                    })
                }
            })
        }.toString(2)
    }

    @Synchronized
    fun validateBackup(raw: String): BackupValidation {
        val parsed = runCatching {
            require(raw.toByteArray(Charsets.UTF_8).size <= MAX_BACKUP_BYTES) { "Sicherungsdatei ist zu groß oder beschädigt." }
            val root = JSONObject(raw)
            require(root.optString("format") == BACKUP_FORMAT) { "Datei ist keine TRAKUNA-Sicherung." }
            require(root.optInt("schemaVersion", -1) == BACKUP_SCHEMA_VERSION) { "Sicherungsversion wird nicht unterstützt." }
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
        val saved = prefs.edit().putString(KEY_RECORDS_ENCRYPTED, encrypted).remove(KEY_RECORDS).commit()
        return if (saved) BackupRestoreResult.Restored(records.size)
        else BackupRestoreResult.Rejected("Daten konnten nicht dauerhaft gespeichert werden. Vorhandene Daten wurden nicht absichtlich gelöscht.")
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
            })
        }
    }.toString()

    @Synchronized
    fun exportCsv(): String {
        val header = listOf("ID", "Sendungsnummer", "Paketdienst", "Vorgang", "Status", "Empfänger", "Notiz", "Erstellt (UTC)", "Aktualisiert (UTC)")
        val rows = readRecords().getOrElse { error("Lokaler Datenspeicher ist beschädigt; CSV wurde nicht erstellt.") }.map { record ->
            listOf(
                record.id,
                record.trackingNumber,
                record.carrier,
                record.flow.name,
                record.status.name,
                record.recipient,
                record.note,
                ShipmentRules.formatTimestampForExport(record.createdAt),
                ShipmentRules.formatTimestampForExport(record.updatedAt)
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
        private const val BACKUP_FORMAT = "TRAKUNA_BACKUP"
        private const val BACKUP_SCHEMA_VERSION = 1
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
        const val MAX_BACKUP_BYTES = 2 * 1024 * 1024
        private val TERMINAL_STATUSES = setOf(
            ShipmentStatus.HANDED_TO_CUSTOMER,
            ShipmentStatus.HANDED_TO_CARRIER
        )
    }
}


/** Pure parcel workflow rules shared by the Android store and JVM unit tests. */
object ShipmentRules {
    fun formatTimestamp(timestamp: Long, zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()): String =
        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", java.util.Locale.GERMANY)
            .withZone(zoneId)
            .format(java.time.Instant.ofEpochMilli(timestamp))

    fun formatTimestampForExport(timestamp: Long): String =
        java.time.Instant.ofEpochMilli(timestamp).toString()

    fun csvSafeCell(value: String): String {
        // Prefix spreadsheet formula starters so untrusted notes/names cannot execute as formulas.
        val candidate = value.trimStart()
        val formulaPrefix = candidate.firstOrNull()?.let { it == '=' || it == '+' || it == '-' || it == '@' || it == '\t' || it == '\r' } == true
        val safeValue = if (formulaPrefix) "'$value" else value
        return "\"" + safeValue.replace("\"", "\"\"") + "\""
    }

    fun isValidRecord(record: ShipmentRecord): Boolean =
        record.id.isNotBlank() && record.id.length <= 80 &&
            isValidTrackingNumber(record.trackingNumber) &&
            record.carrier.length <= 80 && record.recipient.length <= 100 && record.note.length <= 500 &&
            record.createdAt > 0L && record.updatedAt >= record.createdAt &&
            isStatusValidForFlow(record.flow, record.status)

    fun normalizeTrackingNumber(raw: String): String =
        raw.trim().replace(Regex("\\s+"), "")

    fun normalizeCarrierLabel(raw: String): String = raw.trim().take(80)

    fun resolveCarrierLabel(manual: String, trackingNumber: String): String =
        normalizeCarrierLabel(manual).ifBlank { identifyCarrier(trackingNumber).orEmpty() }

    fun isValidTrackingNumber(value: String): Boolean =
        value.length in 6..40 && value.any(Char::isDigit) &&
            value.all { it.isLetterOrDigit() || it == '-' }

    /**
     * Best-effort carrier hint only. These patterns are not authoritative and must
     * never be used to select a paid shipping product or confirm carrier acceptance.
     */
    fun identifyCarrier(raw: String): String? {
        val value = normalizeTrackingNumber(raw).uppercase()
        return when {
            Regex("^1Z[A-Z0-9]{16}$").matches(value) -> "UPS"
            Regex("^[A-Z]{2}[0-9]{9}AT$").matches(value) -> "Österreichische Post"
            value.startsWith("JVGL") || value.startsWith("JJD") -> "DHL Paket"
            value.startsWith("JD") && value.length in 12..30 -> "DHL Paket"
            value.matches(Regex("^[0-9]{13}$")) -> "Österreichische Post"
            value.matches(Regex("^[0-9]{14}$")) -> "DPD"
            value.matches(Regex("^[0-9]{11,12}$")) -> "GLS"
            value.matches(Regex("^[0-9]{10}$")) || value.matches(Regex("^[0-9]{20}$")) -> "DHL Paket"
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

    fun hasNoDuplicateActiveShipments(records: List<ShipmentRecord>): Boolean {
        val activeKeys = records
            .filter { it.status !in setOf(ShipmentStatus.HANDED_TO_CUSTOMER, ShipmentStatus.HANDED_TO_CARRIER) }
            .map { it.trackingNumber.uppercase() to it.flow }
        return activeKeys.distinct().size == activeKeys.size
    }

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
