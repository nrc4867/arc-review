package dev.chieppa.arc.git.parse

import dev.chieppa.arc.git.repo.LocalBranch

/** Reads `git for-each-ref` output written with [FORMAT]. */
object BranchListParser {
    private const val FIELD = '\u001f'
    private const val RECORD = '\u001e'
    private const val FIELDS = 3

    /** The `--format` that [parse] reads: short name, last commit's author and commit time, field and record separated. */
    const val FORMAT = "--format=%(refname:short)%1f%(authorname)%1f%(committerdate:unix)%1e"

    fun parse(text: String): List<LocalBranch> = text.split(RECORD).mapNotNull { record ->
        val fields = record.trim('\n', '\r').split(FIELD)
        if (fields.size != FIELDS || fields[0].isEmpty()) return@mapNotNull null
        LocalBranch(fields[0], fields[1], fields[2].toLongOrNull() ?: 0L)
    }
}
