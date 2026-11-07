package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.Source

/** The ways a pasted link can open, as the switcher lists them under "Open as". */
sealed interface OpenAs {
    val source: Source

    /** The source the link names, added to the review. */
    data class AsSource(override val source: Source) : OpenAs

    /** The whole pull request the link's commit sits in, added to the review. */
    data class WholePull(override val source: Source.PullRequest) : OpenAs

    /** A new review holding the source. */
    data class NewReview(override val source: Source) : OpenAs

    companion object {
        /** The ways [target] can open; none when no clone here holds it. */
        fun of(target: PastedTarget): List<OpenAs> {
            val source = target.source ?: return emptyList()
            return listOfNotNull(
                AsSource(source),
                target.pullRequest?.takeIf { target.withinPull }?.let(::WholePull),
                NewReview(source),
            )
        }
    }
}
