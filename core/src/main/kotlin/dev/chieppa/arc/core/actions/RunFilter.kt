package dev.chieppa.arc.core.actions

/**
 * A saved list of runs under a name: the person's own runs when [mine], else every polled run [query] matches (see
 * [RunQuery]). [id] stays while the name and query change.
 */
data class RunFilter(val id: String, val name: String, val query: String = "", val mine: Boolean = false) {
    init {
        require(id.isNotBlank()) { "A run filter id is not blank" }
    }
}
