package dev.chieppa.arc.index.resolve

/** What each clue adds to a candidate's score. */
object ScoreWeights {
    const val SAME_FILE = 10
    const val IMPORTED = 8
    const val SAME_PACKAGE = 6
    const val WILDCARD_IMPORT = 4
    const val SAME_REPO = 2
    const val KIND_MATCH = 3
    const val KIND_MISMATCH = -6
    const val ARITY_MATCH = 3
    const val ARITY_FITS = 1
    const val ARITY_MISMATCH = -3
    const val SQL_SCHEMA = 4
    const val MAIN_OVER_TEST = 1
}
