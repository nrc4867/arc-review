package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.mark.PatchFingerprint
import dev.chieppa.arc.core.mark.PatchHash
import java.io.ByteArrayOutputStream

/**
 * A file's fingerprint from its section of a `-U0` patch made with [dev.chieppa.arc.git.process.GitSettings.DIFF_OPTIONS]:
 * the header's mode, rename, copy and binary lines make the meta, and each hunk's added, removed and `\` lines, as raw
 * bytes, make the hunks. Context, line numbers and hunk headings are left out.
 */
object PatchFingerprinter {
    private val META_PREFIXES = listOf("old mode ", "new mode ", "new file mode ", "deleted file mode ", "rename from ", "copy from ")
    private const val INDEX_PREFIX = "index "
    private const val BINARY_PREFIX = "Binary files "
    private const val HUNK_PREFIX = "@@"
    private const val META_HEAD = "M"
    private const val NEWLINE = '\n'.code.toByte()
    private val KEPT = setOf('+'.code.toByte(), '-'.code.toByte(), '\\'.code.toByte())

    fun fingerprint(section: ByteArray): PatchFingerprint {
        val meta = mutableListOf<String>()
        val hunks = mutableListOf<MutableList<ByteArray>>()
        var blobs: Pair<String, String>? = null
        for (line in lines(section)) {
            if (hunks.isEmpty()) {
                val text = line.toString(Charsets.UTF_8)
                when {
                    text.startsWith(HUNK_PREFIX) -> hunks += mutableListOf<ByteArray>()
                    META_PREFIXES.any(text::startsWith) -> meta += text
                    text.startsWith(INDEX_PREFIX) -> blobs = indexBlobs(text)
                    text.startsWith(BINARY_PREFIX) -> meta += "binary ${blobs?.first.orEmpty()} ${blobs?.second.orEmpty()}"
                }
            } else if (line.size >= HUNK_PREFIX.length && line[0] == '@'.code.toByte() && line[1] == '@'.code.toByte()) {
                hunks += mutableListOf<ByteArray>()
            } else if (line.isNotEmpty() && line[0] in KEPT) {
                hunks.last() += line
            }
        }
        val metaText = META_HEAD + "\n" + meta.joinToString("\n")
        val exact = ByteArrayOutputStream()
        exact.write(META_HEAD.toByteArray())
        exact.write(NEWLINE.toInt())
        exact.write(meta.joinToString("\n").toByteArray(Charsets.UTF_8))
        for (hunk in hunks.filter { it.isNotEmpty() }) {
            exact.write(NEWLINE.toInt())
            exact.write(HUNK_PREFIX.toByteArray())
            for (line in hunk) {
                exact.write(NEWLINE.toInt())
                exact.write(line)
            }
        }
        return PatchFingerprint(
            meta = PatchHash(sha256Hex(metaText.toByteArray(Charsets.UTF_8))),
            exact = PatchHash(sha256Hex(exact.toByteArray())),
            hunks = hunks.map { PatchHash(sha256Hex(joinLines(it))) },
        )
    }

    private fun indexBlobs(text: String): Pair<String, String>? {
        val range = text.removePrefix(INDEX_PREFIX).substringBefore(' ')
        val parts = range.split("..")
        return if (parts.size == 2) parts[0] to parts[1] else null
    }

    private fun joinLines(lines: List<ByteArray>): ByteArray {
        val out = ByteArrayOutputStream()
        lines.forEachIndexed { i, line ->
            if (i > 0) out.write(NEWLINE.toInt())
            out.write(line)
        }
        return out.toByteArray()
    }

    /** [bytes] split on LF, each line without its LF; a CR stays. */
    private fun lines(bytes: ByteArray): List<ByteArray> = buildList {
        var start = 0
        for (i in bytes.indices) {
            if (bytes[i] == NEWLINE) {
                add(bytes.copyOfRange(start, i))
                start = i + 1
            }
        }
        if (start < bytes.size) add(bytes.copyOfRange(start, bytes.size))
    }
}
