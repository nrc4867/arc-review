package dev.chieppa.arc.core.watch

/**
 * The saved filters and notification rules, which every review shares, and when each filter's results were last
 * looked at, as epoch milliseconds by filter id. Every change returns a new watchlist.
 */
data class Watchlist(
    val filters: List<SavedFilter> = emptyList(),
    val rules: List<NotifyRule> = emptyList(),
    val seen: Map<String, Long> = emptyMap(),
) {
    fun filter(id: String): SavedFilter? = filters.firstOrNull { it.id == id }

    /** The watchlist with [filter] added at the end, or in place of the filter with its id. */
    fun withFilter(filter: SavedFilter): Watchlist =
        if (filters.any { it.id == filter.id }) copy(filters = filters.map { if (it.id == filter.id) filter else it }) else copy(filters = filters + filter)

    /** The watchlist without the filter [id], its rules and when it was seen. */
    fun withoutFilter(id: String): Watchlist = copy(
        filters = filters.filterNot { it.id == id },
        rules = rules.filterNot { it is NotifyRule.NewInFilter && it.filter == id },
        seen = seen - id,
    )

    /** The watchlist with [rule] added at the end, or in place of the rule with its id. */
    fun withRule(rule: NotifyRule): Watchlist =
        if (rules.any { it.id == rule.id }) copy(rules = rules.map { if (it.id == rule.id) rule else it }) else copy(rules = rules + rule)

    fun withoutRule(id: String): Watchlist = copy(rules = rules.filterNot { it.id == id })

    /** The watchlist with the filter [id] looked at [at]. */
    fun seen(id: String, at: Long): Watchlist = copy(seen = seen + (id to at))

    /** An id for a new filter or rule, starting with [prefix], that none here has. */
    fun nextId(prefix: String): String {
        val taken = filters.map { it.id }.toSet() + rules.map { it.id }
        return generateSequence(1) { it + 1 }.map { "$prefix$it" }.first { it !in taken }
    }

    companion object {
        const val FILTER_PREFIX = "f"
        const val RULE_PREFIX = "r"
    }
}
