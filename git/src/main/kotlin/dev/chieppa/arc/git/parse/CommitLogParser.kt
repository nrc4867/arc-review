package dev.chieppa.arc.git.parse

import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.core.change.ObjectId

/** Reads `git log` output written with [FORMAT]. */
object CommitLogParser {
    private const val FIELD = '\u001f'
    private const val RECORD = '\u001e'
    private const val FIELDS = 4

    /** The `--format` that [parse] reads: id, subject, author name and author time, field and record separated. */
    const val FORMAT = "--format=%H%x1f%s%x1f%an%x1f%at%x1e"

    fun parse(text: String): List<CommitInfo> = text.split(RECORD).mapNotNull { record ->
        val fields = record.trim('\n', '\r').split(FIELD)
        if (fields.size != FIELDS) return@mapNotNull null
        CommitInfo(ObjectId(fields[0]), fields[1], fields[2], fields[3].toLongOrNull() ?: 0L)
    }
}
