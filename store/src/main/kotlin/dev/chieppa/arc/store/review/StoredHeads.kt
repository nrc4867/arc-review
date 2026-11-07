package dev.chieppa.arc.store.review

import kotlinx.serialization.Serializable

/** The heads Arc read a source at, by the source's ref, oldest first. */
@Serializable
data class StoredHeads(val source: String, val heads: List<StoredHead> = emptyList())

/** One head as written: its commit, the merge-base it was compared from, and when it was read (epoch seconds). */
@Serializable
data class StoredHead(val head: String, val base: String, val at: Long)
