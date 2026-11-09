package dev.chieppa.arc.git.parse

import dev.chieppa.arc.git.repo.LocalBranch

/** Reads `git for-each-ref` output written with [FORMAT] or [formatAhead]. */
object BranchListParser {
    private const val FIELD = '\u001f'
    private const val RECORD = '\u001e'
    private const val FIELDS = 3
    private const val FIELDS_WITH_AHEAD = 4
    private const val FIELDS_FORMAT = "%(refname:short)%1f%(authorname)%1f%(committerdate:unix)"
    private const val RECORD_FORMAT = "%1e"

    /** The `--format` that [parse] reads: short name, last commit's author and commit time, field and record separated. */
    const val FORMAT = "--format=$FIELDS_FORMAT$RECORD_FORMAT"

    /** [FORMAT] with how many commits each branch has that [base] does not; it needs git 2.41 or later. */
    fun formatAhead(base: String): String = "--format=$FIELDS_FORMAT%1f%(ahead-behind:$base)$RECORD_FORMAT"

    fun parse(text: String): List<LocalBranch> = text.split(RECORD).mapNotNull { record ->
        val fields = record.trim('\n', '\r').split(FIELD)
        if ((fields.size != FIELDS && fields.size != FIELDS_WITH_AHEAD) || fields[0].isEmpty()) return@mapNotNull null
        val ahead = fields.getOrNull(FIELDS)?.substringBefore(' ')?.toIntOrNull()
        LocalBranch(fields[0], fields[1], fields[2].toLongOrNull() ?: 0L, ahead)
    }
}
