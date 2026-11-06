package dev.chieppa.arc.core.source

/** The marks that separate a source ref's parts. */
internal object SourceRefSyntax {
    const val PULL_REQUEST = '#'
    const val COMMIT = '@'
    const val BRANCH = ':'
    const val RANGE = ".."
    const val BASE = "..."
}
