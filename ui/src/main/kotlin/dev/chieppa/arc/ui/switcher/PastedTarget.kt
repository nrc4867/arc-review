package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.Source

/**
 * What the switcher's field names when it holds a link or a ref rather than a search: the source as [named] (in repo
 * `owner/name` for a link), that [source] in a local clone, null when no clone here comes from its repo, the pull
 * request it sits in, [pullRequest], and whether it was a pasted [link].
 */
data class PastedTarget(val named: Source, val source: Source?, val pullRequest: Source.PullRequest?, val link: Boolean) {
    /** Whether the pull request it sits in is another source than itself, as for a pull request's commit. */
    val withinPull: Boolean get() = pullRequest != null && pullRequest != source
}
