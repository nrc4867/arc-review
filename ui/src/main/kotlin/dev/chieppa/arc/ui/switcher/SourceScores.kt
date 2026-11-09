package dev.chieppa.arc.ui.switcher

import dev.chieppa.stretch.frame.fuzzyMatch

/**
 * How well a query matches a source row, lower is better, by [fuzzyMatch]: its title or ref as they are, and its other
 * facts (who made it, its branch, its repo) [ASIDE] worse, so a title match ranks first.
 */
object SourceScores {
    /** How much worse a match on a row's other facts ranks than one on its title or ref. */
    const val ASIDE = 5

    fun of(query: String, item: SwitcherItem): Int? = when (item) {
        is SwitcherItem.InWorkspace -> best(listOf(item.title, item.source.ref), emptyList(), query)
        is SwitcherItem.FollowedPull -> best(
            listOf(item.pull.title, item.source.ref, NUMBER + item.pull.number),
            listOfNotNull(item.pull.author, item.pull.headBranch, item.pull.repo.fullName),
            query,
        )
        is SwitcherItem.FollowedBranch -> best(listOf(item.branch.name), listOf(item.branch.author, item.source.repo.value), query)
        is SwitcherItem.Recent -> best(listOf(item.title, item.source.ref), emptyList(), query)
        is SwitcherItem.Changed, is SwitcherItem.AtHead -> null
    }

    private fun best(main: List<String>, aside: List<String>, query: String): Int? = (
        main.mapNotNull { fuzzyMatch(query, it)?.score } + aside.mapNotNull { fuzzyMatch(query, it)?.score?.plus(ASIDE) }
        ).minOrNull()

    private const val NUMBER = "#"
}
