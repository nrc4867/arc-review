package dev.chieppa.arc.host.github.actions

/** A commit that ships with a deployment: its sha, its title, and the pull request it came from when the title says. */
data class ShippedChange(val sha: String, val title: String, val pull: Int?)
