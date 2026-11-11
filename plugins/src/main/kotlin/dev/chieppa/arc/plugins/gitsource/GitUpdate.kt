package dev.chieppa.arc.plugins.gitsource

import dev.chieppa.arc.plugins.manifest.Capability

/**
 * A newer commit of a git source, never applied by itself: its [sha], the [version] its plugins declare, and the
 * capabilities its manifests [asks] for beyond what each plugin has been approved for.
 */
data class GitUpdate(val sha: String, val version: String?, val asks: List<Capability>)
