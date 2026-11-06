package dev.chieppa.arc.git.process

/** How Arc runs git: the program, the environment it adds and the options that pin a diff's output. */
object GitSettings {
    const val PROGRAM = "git"

    /**
     * Added to every command's environment. Git must never fetch a missing blob on its own, one at a time, from a
     * partial clone: a read that needs one fails, and Arc fetches whole trees in one batch first.
     */
    val ENVIRONMENT: Map<String, String> = mapOf(
        "GIT_NO_LAZY_FETCH" to "1",
        "GIT_TERMINAL_PROMPT" to "0",
        "GIT_OPTIONAL_LOCKS" to "0",
        "LC_ALL" to "C",
    )

    /** Config for every command, so the person's own settings cannot change what Arc reads. */
    val CONFIG: List<String> = listOf("-c", "core.quotePath=false", "-c", "color.ui=false", "-c", "diff.noprefix=false")

    /**
     * Options that fix a diff's output whatever the person's config says: the patch it gives is what a file's
     * fingerprint is hashed from.
     */
    val DIFF_OPTIONS: List<String> = listOf(
        "--no-ext-diff", "--no-textconv", "--no-color", "--histogram", "--full-index", "--no-abbrev", "-M50%",
        "--src-prefix=a/", "--dst-prefix=b/",
    )

    /** Exit code git uses for "differences found" or "conflicts", which is not a failure for diff and merge-tree. */
    const val EXIT_SOME = 1

    /** The object id of the empty tree, the base of a repo's first commit. */
    const val EMPTY_TREE_SHA1 = "4b825dc642cb6eb9a060e54bf8d69288fbee4904"
}
