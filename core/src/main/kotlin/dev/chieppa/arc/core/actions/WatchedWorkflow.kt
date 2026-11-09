package dev.chieppa.arc.core.actions

/** A workflow watched in [repo] (`owner/name`) by its file, such as `.github/workflows/nightly.yml`. */
data class WatchedWorkflow(val repo: String, val path: String)
