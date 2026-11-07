package dev.chieppa.arc.git.revision

/** A head the host says a pull request has had: its commit [oid], and when it was pushed (epoch seconds) when the host knows. */
data class PushedHead(val oid: String, val pushedAt: Long? = null)
