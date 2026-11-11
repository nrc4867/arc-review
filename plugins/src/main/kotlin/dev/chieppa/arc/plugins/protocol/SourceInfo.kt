package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * A source under review as a plugin sees it: its [ref] (`api#412`), its [kind] (`pull`, `commit`, `range`, `branch`),
 * its [repo] and the folder of the repo's clone, its [worktree], when Arc knows it.
 */
@Serializable
data class SourceInfo(val ref: String, val kind: String, val repo: RepoInfo, val worktree: String? = null)
