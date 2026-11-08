package dev.chieppa.arc.core.start

/** A [contribution] with the id of who made it: [ARC] for Arc's own, else a plugin's id. */
data class StartContributor(val id: String, val contribution: StartContribution) {
    companion object {
        /** The id Arc's own cards are contributed under. */
        const val ARC = "arc"
    }
}
