package dev.chieppa.arc.core.actions

/**
 * The person's run filters and what makes runs theirs: which of the Mine rules are on, the workflows and runs they
 * watch (each run with when it was watched, in epoch milliseconds), which notifications are on, and the artifact name
 * each repo's runs upload their summary under, where it is not [SUMMARY_ARTIFACT]. Every change returns a new one.
 */
data class RunWatchlist(
    val filters: List<RunFilter> = emptyList(),
    val mineRules: Set<MineRule> = MineRule.entries.toSet(),
    val workflows: List<WatchedWorkflow> = emptyList(),
    val runs: Map<RunRef, Long> = emptyMap(),
    val notify: Set<RunNotify> = RunNotify.DEFAULTS,
    val summaryArtifacts: Map<String, String> = emptyMap(),
) {
    fun filter(id: String): RunFilter? = filters.firstOrNull { it.id == id }

    /** The watchlist with [filter] added at the end, or in place of the filter with its id. */
    fun withFilter(filter: RunFilter): RunWatchlist =
        if (filters.any { it.id == filter.id }) copy(filters = filters.map { if (it.id == filter.id) filter else it }) else copy(filters = filters + filter)

    fun withoutFilter(id: String): RunWatchlist = copy(filters = filters.filterNot { it.id == id })

    /** The watchlist with [rule] switched [on] or off. */
    fun withRule(rule: MineRule, on: Boolean): RunWatchlist = copy(mineRules = if (on) mineRules + rule else mineRules - rule)

    fun withNotify(kind: RunNotify, on: Boolean): RunWatchlist = copy(notify = if (on) notify + kind else notify - kind)

    /** The watchlist watching [run] since [at], or not when not [on]. */
    fun watchingRun(run: RunRef, on: Boolean, at: Long): RunWatchlist = copy(runs = if (on) runs + (run to at) else runs - run)

    fun watchingWorkflow(workflow: WatchedWorkflow, on: Boolean): RunWatchlist =
        copy(workflows = if (on) (workflows - workflow) + workflow else workflows - workflow)

    /** Without the runs watched before [before], in epoch milliseconds. */
    fun forgetRunsBefore(before: Long): RunWatchlist = copy(runs = runs.filterValues { it >= before })

    /** The artifact [repo]'s runs upload their Markdown summary under. */
    fun summaryArtifact(repo: String): String = summaryArtifacts[repo.lowercase()] ?: SUMMARY_ARTIFACT

    fun withSummaryArtifact(repo: String, name: String): RunWatchlist = copy(summaryArtifacts = summaryArtifacts + (repo.lowercase() to name))

    /** An id for a new filter that none here has. */
    fun nextId(): String = generateSequence(1) { it + 1 }.map { "$FILTER_PREFIX$it" }.first { id -> filters.none { it.id == id } }

    companion object {
        const val FILTER_PREFIX = "rf"
        const val MINE = "mine"
        const val WAITING = "waiting"
        const val SUMMARY_ARTIFACT = "summary"

        /** What a first run starts with: Mine, and the runs waiting on the person, under [mineName] and [waitingName]. */
        fun seeds(mineName: String, waitingName: String): RunWatchlist = RunWatchlist(
            filters = listOf(
                RunFilter(MINE, mineName, mine = true),
                RunFilter(WAITING, waitingName, "${RunQuery.IS}:${RunQuery.WAITING_ON_ME}"),
            ),
        )
    }
}
