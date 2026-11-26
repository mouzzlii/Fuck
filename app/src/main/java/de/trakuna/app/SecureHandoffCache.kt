package de.trakuna.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Encrypts the last handoff value independently from the non-sensitive partner-app choice.
 * Legacy plaintext values are removed only after a successful encrypted commit.
 */
class SecureHandoffCache(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    @Volatile private var initializationError: String? = null
    @Volatile private var operationError: String? = null

    init {
        migrateLegacyValue()
    }

    @Synchronized
    fun save(trackingNumber: String, carrier: String?): Boolean {
        if (initializationError != null) return false
        val result = runCatching {
            require(ShipmentRules.isValidTrackingNumber(trackingNumber)) { "Sendungsnummer für die Übergabe ist ungültig." }
            require(carrier.orEmpty().length <= 80) { "Paketdienstangabe ist zu lang." }
            val payload = JSONObject().apply {
                put("trackingNumber", trackingNumber)
                put("carrier", carrier.orEmpty())
                put("savedAt", System.currentTimeMillis())
            }.toString()
            val encrypted = encrypt(payload)
            prefs.edit()
                .putString(KEY_ENCRYPTED, encrypted)
                .remove(KEY_LEGACY_NUMBER)
                .remove(KEY_LEGACY_CARRIER)
                .remove(KEY_LEGACY_SAVED_AT)
                .commit()
        }
        operationError = when {
            result.isFailure -> result.exceptionOrNull()?.message ?: "Übergabedaten konnten nicht verschlüsselt werden."
            result.getOrDefault(false).not() -> "Übergabedaten konnten nicht dauerhaft gespeichert werden."
            else -> null
        }
        return result.getOrDefault(false)
    }

    /** Returns a precise failure instead of disguising damaged cache data as an empty cache. */
    fun failureReason(): String? = initializationError ?: operationError

    @Synchronized
    fun trackingNumber(): String {
        check(initializationError == null) {
            initializationError ?: "Übergabe-Cache konnte nicht initialisiert werden."
        }
        val encrypted = prefs.getString(KEY_ENCRYPTED, null) ?: return ""
        return try {
            decodePayload(decrypt(encrypted)).getString("trackingNumber")
        } catch (error: Exception) {
            operationError = error.message ?: "Übergabe-Cache ist beschädigt oder nicht lesbar."
            throw IllegalStateException(operationError, error)
        }
    }

    private fun migrateLegacyValue() {
        runCatching {
            val encrypted = prefs.getString(KEY_ENCRYPTED, null)
            val legacy = prefs.getString(KEY_LEGACY_NUMBER, null)
            if (encrypted != null) {
                decodePayload(decrypt(encrypted))
                if (legacy != null && !prefs.edit()
                        .remove(KEY_LEGACY_NUMBER)
                        .remove(KEY_LEGACY_CARRIER)
                        .remove(KEY_LEGACY_SAVED_AT)
                        .commit()
                ) error("Alte Übergabedaten konnten nicht entfernt werden.")
            } else if (!legacy.isNullOrBlank()) {
                val payload = JSONObject().apply {
                    put("trackingNumber", legacy)
                    put("carrier", prefs.getString(KEY_LEGACY_CARRIER, "").orEmpty())
                    put("savedAt", prefs.getLong(KEY_LEGACY_SAVED_AT, 0L))
                }.toString()
                decodePayload(payload)
                if (!prefs.edit().putString(KEY_ENCRYPTED, encrypt(payload))
                        .remove(KEY_LEGACY_NUMBER)
                        .remove(KEY_LEGACY_CARRIER)
                        .remove(KEY_LEGACY_SAVED_AT)
                        .commit()
                ) error("Übergabedaten konnten nicht verschlüsselt gespeichert werden.")
            }
        }.onFailure {
            initializationError = it.message ?: "Übergabedaten konnten nicht sicher initialisiert werden."
        }
    }

    private fun decodePayload(raw: String): JSONObject {
        val payload = JSONObject(raw)
        val number = payload.optString("trackingNumber")
        val carrier = payload.optString("carrier")
        val savedAt = payload.optLong("savedAt", -1L)
        require(ShipmentRules.isValidTrackingNumber(number)) { "Gespeicherte Übergabenummer ist ungültig." }
        require(carrier.length <= 80) { "Gespeicherte Paketdienstangabe ist ungültig." }
        require(savedAt >= 0L) { "Zeitpunkt der Übergabe ist ungültig." }
        return payload
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
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

    private fun encrypt(value: String): String {
        val bytes = value.toByteArray(Charsets.UTF_8)
        require(bytes.size <= MAX_PAYLOAD_BYTES) { "Übergabedaten sind zu groß." }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv + cipher.doFinal(bytes), Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        require(value.length <= MAX_ENCODED_CHARS) { "Verschlüsselte Übergabedaten sind zu groß." }
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        require(bytes.size in (IV_BYTES + TAG_BYTES)..(MAX_PAYLOAD_BYTES + IV_BYTES + TAG_BYTES)) {
            "Verschlüsselte Übergabedaten sind beschädigt."
        }
        val iv = bytes.copyOfRange(0, IV_BYTES)
        val encrypted = bytes.copyOfRange(IV_BYTES, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), javax.crypto.spec.GCMParameterSpec(128, iv))
        return decodeStrictUtf8(cipher.doFinal(encrypted))
    }

    private companion object {
        const val PREFS = "trakuna_handoff"
        const val KEY_ENCRYPTED = "tracking_number_encrypted_v1"
        const val KEY_LEGACY_NUMBER = "tracking_number"
        const val KEY_LEGACY_CARRIER = "carrier"
        const val KEY_LEGACY_SAVED_AT = "saved_at"
        const val KEY_ALIAS = "de.trakuna.app.handoff.aes.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BYTES = 16
        const val MAX_PAYLOAD_BYTES = 4096
        const val MAX_ENCODED_CHARS = 6000
    }
}
