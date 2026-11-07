package dev.chieppa.arc.git.revision

import dev.chieppa.arc.core.change.ObjectId

/** The private refs Arc keeps every revision's head under, so a rebase or force push cannot lose it. */
object RevisionRefNames {
    private const val PREFIX = "refs/arc/revision/"

    /** The ref that keeps [head]. */
    fun pin(head: ObjectId): String = PREFIX + head.value
}
