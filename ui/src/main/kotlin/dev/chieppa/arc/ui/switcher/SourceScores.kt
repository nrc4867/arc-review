package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.Source
import dev.chieppa.stretch.frame.fuzzyMatch

/**
 * How well a query matches a source row, lower is better, by [fuzzyMatch]: its title or ref as they are, and its other
 * facts (who made it, its branch, its repo) [ASIDE] worse, so a title match ranks first. A commit also matches by the
 * start of its sha, and a branch with nothing ahead of the default branch ranks [ASIDE] worse.
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
            ?.plus(if (item.branch.ahead == 0) ASIDE else 0)
        is SwitcherItem.FollowedCommit -> shaScore(query, item) ?: best(listOf(item.commit.subject), listOf(item.commit.author), query)
        is SwitcherItem.Recent -> best(listOf(item.title, item.source.ref), emptyList(), query)
        is SwitcherItem.Changed, is SwitcherItem.AtHead, is SwitcherItem.GitHubRepository -> null
    }

    /** 0 when [query] is the start of the commit's sha, or names its range by the starts of both ends. */
    private fun shaScore(query: String, item: SwitcherItem.FollowedCommit): Int? {
        val named = CommitQuery.read(query) ?: return null
        val matches = when (val source = item.source) {
            is Source.Commit -> named.from == null && source.sha.value.startsWith(named.to)
            is Source.CommitRange -> named.from != null && source.from.value.startsWith(named.from) && source.to.value.startsWith(named.to)
            else -> false
        }
        return if (matches) 0 else null
    }

    private fun best(main: List<String>, aside: List<String>, query: String): Int? = (
        main.mapNotNull { fuzzyMatch(query, it)?.score } + aside.mapNotNull { fuzzyMatch(query, it)?.score?.plus(ASIDE) }
        ).minOrNull()

    private const val NUMBER = "#"
}
