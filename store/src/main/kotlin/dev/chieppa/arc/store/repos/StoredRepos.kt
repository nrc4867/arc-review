package dev.chieppa.arc.store.repos

import kotlinx.serialization.Serializable

/** The repo registry as written: each repo's name and the folder of its clone. */
@Serializable
internal data class StoredRepos(val repos: List<StoredRepo> = emptyList())

/** One repo in the registry. */
@Serializable
internal data class StoredRepo(val name: String, val path: String)
