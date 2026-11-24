package de.trakuna.app

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Arrays
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-protected, portable backup envelope.
 *
 * AES-256-GCM encrypts the backup; PBKDF2-HMAC-SHA256 derives a per-backup key
 * from a user passphrase and random salt. The salt, nonce and KDF parameters
 * are authenticated as additional data. No device Keystore key is required,
 * so a backup can be restored on another authorized device.
 */
object BackupCrypto {
    private const val PREFIX = "TRAKUNA-ENCRYPTED-BACKUP-V1\n"
    private const val VERSION = 1
    private const val ITERATIONS = 600_000
    private val SUPPORTED_ITERATIONS = setOf(210_000, ITERATIONS)
    private const val KEY_BITS = 256
    private const val SALT_BYTES = 16
    private const val NONCE_BYTES = 12
    private const val TAG_BITS = 128
    private const val HEADER_BYTES = 1 + 4 + SALT_BYTES + NONCE_BYTES
    private const val MIN_PASSWORD_CHARS = 12
    private const val MAX_PASSWORD_CHARS = 128
    private const val MAX_PLAINTEXT_BYTES = 4 * 1024 * 1024
    const val MAX_ENCRYPTED_BACKUP_BYTES = 6 * 1024 * 1024

    private val random = SecureRandom()
    private val prefixBytes = PREFIX.toByteArray(Charsets.US_ASCII)

    fun isEncryptedBackup(raw: String): Boolean = raw.startsWith(PREFIX)

    fun encrypt(plainText: String, passphrase: CharArray): String = try {
        validatePassphrase(passphrase)
        val plain = plainText.toByteArray(Charsets.UTF_8)
        try {
        require(plain.size <= MAX_PLAINTEXT_BYTES) { "Sicherungsdaten überschreiten die zulässige Größe." }

        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val nonce = ByteArray(NONCE_BYTES).also(random::nextBytes)
        val header = ByteBuffer.allocate(HEADER_BYTES)
            .put(VERSION.toByte())
            .putInt(ITERATIONS)
            .put(salt)
            .put(nonce)
            .array()
        val keyBytes = deriveKey(passphrase, salt, ITERATIONS)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(prefixBytes)
            cipher.updateAAD(header)
            val ciphertext = cipher.doFinal(plain)
            val encoded = PREFIX + Base64.getEncoder().encodeToString(header + ciphertext)
            require(encoded.toByteArray(Charsets.UTF_8).size <= MAX_ENCRYPTED_BACKUP_BYTES) {
                "Verschlüsselte Sicherung überschreitet die zulässige Dateigröße."
            }
            encoded
        } finally {
            Arrays.fill(keyBytes, 0)
        }
        } finally {
            Arrays.fill(plain, 0)
        }
    } finally {
        Arrays.fill(passphrase, '\u0000')
    }

    fun decrypt(encryptedText: String, passphrase: CharArray): String = try {
        require(encryptedText.toByteArray(Charsets.UTF_8).size <= MAX_ENCRYPTED_BACKUP_BYTES) {
            "Sicherungsdatei überschreitet die zulässige Größe."
        }
        require(isEncryptedBackup(encryptedText)) { "Datei ist keine verschlüsselte TRAKUNA-Sicherung." }
        validatePassphrase(passphrase)
        val payload = runCatching {
            Base64.getDecoder().decode(encryptedText.substring(PREFIX.length).trim())
        }.getOrElse { throw IllegalArgumentException("Sicherungsdatei ist beschädigt.") }
        require(payload.size > HEADER_BYTES + TAG_BITS / 8) { "Sicherungsdatei ist unvollständig." }

        val header = payload.copyOfRange(0, HEADER_BYTES)
        val buffer = ByteBuffer.wrap(header)
        val version = buffer.get().toInt() and 0xff
        val iterations = buffer.int
        require(version == VERSION && iterations in SUPPORTED_ITERATIONS) {
            "Sicherungsversion oder Verschlüsselungsparameter werden nicht unterstützt."
        }
        val salt = ByteArray(SALT_BYTES)
        buffer.get(salt)
        val nonce = ByteArray(NONCE_BYTES)
        buffer.get(nonce)
        val ciphertext = payload.copyOfRange(HEADER_BYTES, payload.size)
        val keyBytes = deriveKey(passphrase, salt, iterations)
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), GCMParameterSpec(TAG_BITS, nonce))
            cipher.updateAAD(prefixBytes)
            cipher.updateAAD(header)
            val plain = try {
                cipher.doFinal(ciphertext)
            } catch (_: AEADBadTagException) {
                throw IllegalArgumentException("Passwort falsch oder Sicherungsdatei beschädigt.")
            }
            require(plain.size <= MAX_PLAINTEXT_BYTES) { "Entschlüsselte Sicherung überschreitet die zulässige Größe." }
            try {
                String(plain, Charsets.UTF_8)
            } finally {
                Arrays.fill(plain, 0)
            }
        } finally {
            Arrays.fill(keyBytes, 0)
        }
    } finally {
        Arrays.fill(passphrase, '\u0000')
    }

    private fun validatePassphrase(passphrase: CharArray) {
        require(passphrase.size in MIN_PASSWORD_CHARS..MAX_PASSWORD_CHARS) {
            "Sicherungspasswort muss 12 bis 128 Zeichen haben."
        }
    }

    /**
     * PBKDF2-HMAC-SHA256 over an explicitly UTF-8 encoded passphrase.
     * Keeping the byte encoding explicit makes backups portable across Android
     * providers and devices instead of relying on provider-specific PBEKeySpec
     * character conversion.
     */
    internal fun deriveKey(passphrase: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        try {
            require(iterations in 1..1_000_000) { "Ungültige Schlüsselableitungsparameter." }
            val encoder = Charsets.UTF_8.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            val encodedPassword = encoder.encode(CharBuffer.wrap(passphrase))
            val passwordBytes = ByteArray(encodedPassword.remaining())
            encodedPassword.get(passwordBytes)
            if (encodedPassword.hasArray()) Arrays.fill(encodedPassword.array(), 0)

            val derived = ByteArray(KEY_BITS / 8)
            var u = ByteArray(32)
            var next = ByteArray(32)
            try {
                val mac = Mac.getInstance("HmacSHA256")
                mac.init(SecretKeySpec(passwordBytes, "HmacSHA256"))

                // PBKDF2 block index 1, encoded as a four-byte big-endian integer.
                mac.update(salt)
                mac.update(byteArrayOf(0, 0, 0, 1))
                mac.doFinal(u, 0)
                u.copyInto(derived)

                repeat(iterations - 1) {
                    mac.update(u)
                    mac.doFinal(next, 0)
                    for (index in derived.indices) {
                        derived[index] = (derived[index].toInt() xor next[index].toInt()).toByte()
                    }
                    val previous = u
                    u = next
                    next = previous
                }
                return derived
            } catch (failure: Throwable) {
                Arrays.fill(derived, 0)
                throw failure
            } finally {
                Arrays.fill(passwordBytes, 0)
                Arrays.fill(u, 0)
                Arrays.fill(next, 0)
            }
        } finally {
            Arrays.fill(passphrase, '\u0000')
        }
    }
}
