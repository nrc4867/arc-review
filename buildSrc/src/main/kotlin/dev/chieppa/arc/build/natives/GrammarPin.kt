package dev.chieppa.arc.build.natives

/** One line of grammars.tsv: a library's name, the repository and commit it is built from, and its source folder. */
data class GrammarPin(val name: String, val repository: String, val commit: String, val folder: String) {
    /** Whether this is the tree-sitter core rather than a grammar. */
    val isCore: Boolean get() = name == CORE

    /** The library's name before platform naming: `tree-sitter` or `tree-sitter-<name>`. */
    val libraryName: String get() = if (isCore) CORE_LIBRARY else "$CORE_LIBRARY-$name"

    /** The checkout folder's name, shared by every pin of one repository at one commit. */
    val checkoutName: String get() = repository.substringAfterLast('/').removeSuffix(".git") + "-" + commit.take(COMMIT_CHARS)

    private companion object {
        const val CORE = "core"
        const val CORE_LIBRARY = "tree-sitter"
        const val COMMIT_CHARS = 12
    }
}
