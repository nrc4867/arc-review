package dev.chieppa.arc.index

/** The index's limits. */
object IndexLimits {
    /** Files larger than this are left out, as generated or vendored. */
    const val MAX_FILE_BYTES = 1L shl 20

    /** How many decoded files the previews keep at hand. */
    const val PREVIEW_TEXTS = 64

    /** How many revisions' trees the index keeps listed for files opened outside the indexed revisions. */
    const val LISTED_TREES = 8
}
