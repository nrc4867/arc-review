package dev.chieppa.arc.store.workspace

import kotlinx.serialization.Serializable

/** A file mark as written: its source ref, path, kind name, fingerprint hashes and the revisions it was made at. */
@Serializable
data class StoredMark(
    val source: String,
    val path: String,
    val kind: String,
    val meta: String,
    val exact: String,
    val hunks: List<String> = emptyList(),
    val reviewedHead: String? = null,
    val reviewedBase: String? = null,
)
