package dev.chieppa.arc.host.github.actions

/** A file a fork's pull request changes under `.github/`: its path, how it changed, and the patch GitHub shows. */
data class ForkChange(val path: String, val status: String, val patch: String?)
