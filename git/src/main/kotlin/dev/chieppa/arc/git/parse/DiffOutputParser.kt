package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.ChangeStatus
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.parse.NulReader.Companion.NUL
import dev.chieppa.arc.git.parse.NulReader.Companion.TAB

/**
 * Reads the output of `git diff --raw --numstat -z -p`: the raw records, then one numstat record for each, then the
 * patch with one `diff --git` section for each, all in the same order.
 */
object DiffOutputParser {
    private const val RAW_START = ':'.code
    private const val META_FIELDS = 5
    private const val BINARY_COUNT = "-"
    private val SECTION_START = "diff --git ".toByteArray(Charsets.UTF_8)

    fun parse(output: ByteArray): List<DiffFileOutput> {
        val reader = NulReader(output)
        val raws = buildList { while (reader.peek() == RAW_START) add(raw(reader)) }
        val counts = raws.map { counts(reader) }
        reader.skip(NUL)
        val sections = sections(reader.rest())
        return raws.mapIndexed { index, raw ->
            val (added, removed) = counts[index]
            val file = ChangedFile(
                path = if (raw.status == ChangeStatus.Deleted) raw.oldPath else raw.newPath,
                status = raw.status,
                oldPath = if (raw.status == ChangeStatus.Added) null else raw.oldPath,
                added = added,
                removed = removed,
                oldBlob = raw.oldBlob,
                newBlob = raw.newBlob,
            )
            DiffFileOutput(file, sections.getOrElse(index) { ByteArray(0) })
        }
    }

    private fun raw(reader: NulReader): RawChange {
        val meta = reader.field().removePrefix(":").split(' ')
        require(meta.size == META_FIELDS) { "Not a raw diff record: $meta" }
        val status = ChangeStatus.ofGitLetter(meta[4].first()) ?: ChangeStatus.Modified
        val first = reader.field()
        val second = if (status == ChangeStatus.Renamed || status == ChangeStatus.Copied) reader.field() else first
        return RawChange(blob(meta[2]), blob(meta[3]), status, first, second)
    }

    private fun blob(text: String): ObjectId? = ObjectId(text).takeUnless { it.isNull }

    private fun counts(reader: NulReader): Pair<Int?, Int?> {
        val added = reader.until(TAB)
        val removed = reader.until(TAB)
        if (reader.peek() == NUL.toInt()) {
            reader.field()
            reader.field()
            reader.field()
        } else {
            reader.field()
        }
        return added.takeUnless { it == BINARY_COUNT }?.toIntOrNull() to removed.takeUnless { it == BINARY_COUNT }?.toIntOrNull()
    }

    /** The patch split before each `diff --git` line. */
    private fun sections(patch: ByteArray): List<ByteArray> {
        val starts = buildList {
            var lineStart = 0
            while (lineStart < patch.size) {
                if (startsWith(patch, lineStart, SECTION_START)) add(lineStart)
                val end = patch.indexOf('\n'.code.toByte(), lineStart)
                lineStart = if (end < 0) patch.size else end + 1
            }
        }
        return starts.mapIndexed { i, start -> patch.copyOfRange(start, starts.getOrElse(i + 1) { patch.size }) }
    }

    private fun startsWith(bytes: ByteArray, at: Int, prefix: ByteArray): Boolean =
        at + prefix.size <= bytes.size && prefix.indices.all { bytes[at + it] == prefix[it] }

    private fun ByteArray.indexOf(byte: Byte, from: Int): Int {
        for (i in from until size) if (this[i] == byte) return i
        return -1
    }
}
