package dev.chieppa.arc.index.store

/**
 * A blob's bytes as text: UTF-8 unless a byte order mark says UTF-16, as SQL Server Management Studio saves scripts.
 * Null for binary content, which has a NUL byte near the start.
 */
object BlobText {
    private const val BINARY_PROBE_BYTES = 8000
    private const val UTF8_MARK_BYTES = 3
    private const val UTF16_MARK_BYTES = 2

    fun decode(bytes: ByteArray): String? {
        if (bytes.size >= UTF16_MARK_BYTES && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, UTF16_MARK_BYTES, bytes.size - UTF16_MARK_BYTES, Charsets.UTF_16LE)
        }
        if (bytes.size >= UTF16_MARK_BYTES && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, UTF16_MARK_BYTES, bytes.size - UTF16_MARK_BYTES, Charsets.UTF_16BE)
        }
        val utf8Mark = bytes.size >= UTF8_MARK_BYTES && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        val start = if (utf8Mark) UTF8_MARK_BYTES else 0
        for (i in start until minOf(bytes.size, BINARY_PROBE_BYTES)) if (bytes[i] == 0.toByte()) return null
        return String(bytes, start, bytes.size - start, Charsets.UTF_8)
    }
}
