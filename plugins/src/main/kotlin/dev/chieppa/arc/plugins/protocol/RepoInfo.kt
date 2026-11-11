package dev.chieppa.arc.plugins.protocol

import kotlinx.serialization.Serializable

/**
 * The repo a source is in. [id] names it the same in every clone: `github.com/owner/name` for a GitHub repo (the repo a
 * fork's pull request targets), else Arc's own name for it. [name] is Arc's name, and [githubId] GitHub's number for
 * the repo when known, which survives a rename.
 */
@Serializable
data class RepoInfo(val id: String, val name: String, val githubId: Long? = null)
