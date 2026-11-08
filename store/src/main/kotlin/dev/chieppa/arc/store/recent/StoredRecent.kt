package dev.chieppa.arc.store.recent

import kotlinx.serialization.Serializable

/** The recently opened sources as written, newest first. */
@Serializable
internal data class StoredRecent(val sources: List<StoredRecentSource> = emptyList())

/** One recently opened source: its ref, its title where known, and when, in epoch seconds. */
@Serializable
internal data class StoredRecentSource(val ref: String, val title: String? = null, val at: Long)
