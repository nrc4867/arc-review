package dev.chieppa.arc.core.mark

/** What a person said about a changed file. */
enum class MarkKind {
    /** Read and done with. */
    Reviewed,

    /** Left out of the review on purpose: folded out of the tree, out of progress, never sent to a host as viewed. */
    SetAside,
}
