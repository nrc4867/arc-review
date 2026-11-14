package dev.chieppa.arc.host.github.pulls

import java.time.Instant

/** Who the token with fingerprint [key] belonged to, as GitHub answered [at]. */
data class ViewerRecord(val key: String, val viewer: Viewer, val at: Instant)
