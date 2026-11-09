package dev.chieppa.arc.host.github.actions

/** A branch of a repo on GitHub and the commit it points at. */
data class Branch(val name: String, val sha: String)
