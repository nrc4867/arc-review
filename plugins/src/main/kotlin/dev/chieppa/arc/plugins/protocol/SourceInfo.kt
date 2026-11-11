package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * A source under review as a plugin sees it: its [ref] (`api#412`), its [kind] (`pull`, `commit`, `range`, `branch`),
 * its [repo], the folder of the repo's clone, its [worktree], and the commit it is read at, its [head], when Arc knows
 * them.
 */
@Serializable
data class SourceInfo(val ref: String, val kind: String, val repo: RepoInfo, val worktree: String? = null, val head: String? = null)
