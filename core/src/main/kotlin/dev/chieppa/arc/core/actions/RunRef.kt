package dev.chieppa.arc.core.actions

/** One workflow run anywhere: its repo as `owner/name` and GitHub's id for it. */
data class RunRef(val repo: String, val id: Long) {
    override fun toString(): String = "$repo#$id"
}
