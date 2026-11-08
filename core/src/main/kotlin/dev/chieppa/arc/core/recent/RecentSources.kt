package dev.chieppa.arc.core.recent

/** The sources the person opened or put away lately, newest first, at most [LIMIT] and each once. */
data class RecentSources(val sources: List<RecentSource> = emptyList()) {
    /** These with [ref] first, at [at]; its [title] stays as it was when none is given now. */
    fun record(ref: String, title: String?, at: Long): RecentSources {
        val known = sources.firstOrNull { it.ref == ref }
        val entry = RecentSource(ref, title?.ifEmpty { null } ?: known?.title, at)
        return RecentSources((listOf(entry) + sources.filter { it.ref != ref }).take(LIMIT))
    }

    /** These without [ref]. */
    fun remove(ref: String): RecentSources = RecentSources(sources.filter { it.ref != ref })

    companion object {
        const val LIMIT = 12
    }
}
