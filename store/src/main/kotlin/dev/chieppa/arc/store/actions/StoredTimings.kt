package dev.chieppa.arc.store.actions

import kotlinx.serialization.Serializable

/** Each job's recent timings as written, by its key. */
@Serializable
internal data class StoredTimings(val jobs: Map<String, List<StoredSample>> = emptyMap())

@Serializable
internal data class StoredSample(val run: Long, val queued: Long = 0, val steps: Map<String, Long> = emptyMap())
