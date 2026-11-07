package dev.chieppa.arc.core.watch

/**
 * A GitHub search kept under a name: the pull requests [query] finds, polled while the app runs, grouped as [grouping]
 * says. [id] stays while the name and query change.
 */
data class SavedFilter(
    val id: String,
    val name: String,
    val query: String,
    val grouping: FilterGrouping = FilterGrouping.Label,
) {
    init {
        require(id.isNotBlank()) { "A filter id is not blank" }
    }
}
