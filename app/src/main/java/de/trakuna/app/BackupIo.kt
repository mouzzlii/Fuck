package de.trakuna.app

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/**
 * Reads a bounded UTF-8 document without silently replacing malformed byte sequences.
 * Used for untrusted backup files selected through Android's document picker.
 */
internal fun readBoundedUtf8(input: InputStream, maxBytes: Int): String {
    require(maxBytes >= 0) { "Ungültige Größenbegrenzung." }
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var total = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        if (count == 0) {
            val single = input.read()
            if (single < 0) break
            require(total < maxBytes) { "Sicherungsdatei ist größer als 2 MiB und wurde nicht geladen." }
            output.write(single)
            total++
            continue
        }
        require(count <= maxBytes - total) {
            "Sicherungsdatei ist größer als 2 MiB und wurde nicht geladen."
        }
        output.write(buffer, 0, count)
        total += count
    }

    return decodeStrictUtf8(output.toByteArray())
}

/** Decode UTF-8 without replacing malformed bytes with the Unicode replacement character. */
internal fun decodeStrictUtf8(bytes: ByteArray): String {
    val decoder = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
    return decoder.decode(ByteBuffer.wrap(bytes)).toString()
}
