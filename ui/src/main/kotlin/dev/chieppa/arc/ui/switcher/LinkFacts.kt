package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/**
 * What the switcher's card says of a pasted link's source, as far as it could be read: its [title], who made it and
 * when ([time], epoch seconds), its [files] with lines [added] and [removed], how many [commits] it has, and for a
 * commit in a pull request its [position] among the pull request's [of] commits. [pull] is what GitHub said of the
 * pull request it is or sits in.
 */
data class LinkFacts(
    val title: String? = null,
    val author: String? = null,
    val time: Long? = null,
    val files: Int? = null,
    val added: Int = 0,
    val removed: Int = 0,
    val commits: Int? = null,
    val position: Int? = null,
    val of: Int? = null,
    val pull: PullRequestSummary? = null,
)
