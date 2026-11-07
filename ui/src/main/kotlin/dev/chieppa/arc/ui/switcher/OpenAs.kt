package dev.chieppa.arc.ui.switcher

import dev.chieppa.arc.core.source.Source

/** The ways a pasted link can open, as the switcher lists them under "Open as". */
sealed interface OpenAs {
    val source: Source

    /** The source the link names. */
    data class AsSource(override val source: Source) : OpenAs

    /** The whole pull request the link's commit sits in. */
    data class WholePull(override val source: Source.PullRequest) : OpenAs

    companion object {
        /** The ways [target] can open; none when no clone here holds it. */
        fun of(target: PastedTarget): List<OpenAs> {
            val source = target.source ?: return emptyList()
            return listOfNotNull(
                AsSource(source),
                target.pullRequest?.takeIf { target.withinPull }?.let(::WholePull),
            )
        }
    }
}
