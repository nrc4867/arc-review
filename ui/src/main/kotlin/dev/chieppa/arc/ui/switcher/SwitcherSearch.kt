package dev.chieppa.arc.ui.switcher

import dev.chieppa.stretch.frame.matchScore

/**
 * Ranks what the switcher searches against a query, the way the palette ranks: a title or ref starting with the query
 * first, then a word in it, then the query anywhere, then its letters in order. A file ranks by its name before its
 * folder, and its whole path matches only where it holds the query. Each section keeps its best few; the count of all that match stays with it.
 */
object SwitcherSearch {
    fun sections(query: String, scope: SwitcherScope, inputs: SwitcherInputs): List<SwitcherSection> {
        val one = scope != SwitcherScope.All
        val sources = scope != SwitcherScope.Files
        val files = scope != SwitcherScope.Sources
        val heads = if (query.isBlank() && scope != SwitcherScope.Files) emptyList() else inputs.headFiles
        return buildList {
            if (sources) add(section(SectionKind.InWorkspace, rank(query, inputs.sources), if (one) SwitcherLimits.ONE_SCOPE else SwitcherLimits.SOURCES))
            if (files) add(section(SectionKind.Files, rank(query, inputs.files + heads), if (one) SwitcherLimits.ONE_SCOPE else SwitcherLimits.FILES))
            if (sources) add(section(SectionKind.Followed, rank(query, inputs.followed), if (one) SwitcherLimits.ONE_SCOPE else SwitcherLimits.FOLLOWED))
        }.filter { it.items.isNotEmpty() }
    }

    /** How well [item] matches [query], lower is better, or null when it does not. */
    fun score(query: String, item: SwitcherItem): Int? = when (item) {
        is SwitcherItem.InWorkspace -> best(matchScore(query, item.title), matchScore(query, item.source.ref))
        is SwitcherItem.Changed -> fileScore(query, item.file.path)
        is SwitcherItem.AtHead -> fileScore(query, item.path)
        is SwitcherItem.FollowedPull -> best(matchScore(query, item.pull.title), matchScore(query, item.source.ref))
        is SwitcherItem.FollowedBranch -> matchScore(query, item.branch.name)
    }

    private fun <T : SwitcherItem> rank(query: String, items: List<T>): List<T> = items
        .mapIndexedNotNull { index, item -> score(query, item)?.let { Triple(it, index, item) } }
        .sortedWith(compareBy({ it.first }, { it.second }))
        .map { it.third }

    private fun section(kind: SectionKind, ranked: List<SwitcherItem>, limit: Int) = SwitcherSection(kind, ranked.take(limit), ranked.size)

    private fun fileScore(query: String, path: String): Int? = best(
        matchScore(query, path.substringAfterLast('/')),
        matchScore(query, path)?.takeIf { it < LETTERS_IN_ORDER }?.plus(SwitcherLimits.FOLDER_MATCH),
    )

    /** The score of a match by letters in order, which a file's whole path is not searched by: it would match nearly any path. */
    private const val LETTERS_IN_ORDER = 3

    private fun best(a: Int?, b: Int?): Int? = listOfNotNull(a, b).minOrNull()
}
