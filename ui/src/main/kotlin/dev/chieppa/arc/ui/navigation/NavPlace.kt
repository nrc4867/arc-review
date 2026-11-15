package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import kotlin.math.abs

/**
 * A place a person was reading: [line] and [column] on [side] of [path] in [repo] at [revision], shown as its change
 * from [base] when the pane showed one, opened from [source].
 */
data class NavPlace(
    val repo: RepoName,
    val revision: String,
    val path: String,
    val line: Int,
    val column: Int = 0,
    val side: LineSide = LineSide.New,
    val base: String? = null,
    val source: Source? = null,
) {
    /** Whether [other] is in the same file, shown the same way. */
    fun sameFile(other: NavPlace): Boolean = repo == other.repo && revision == other.revision && path == other.path && base == other.base

    /** Whether [other] is close enough to this place to count as the same one, so going back does not stop twice there. */
    fun near(other: NavPlace): Boolean = sameFile(other) && side == other.side && abs(line - other.line) <= NavigationSizes.NEAR_LINES

    /** Where a pane showing this place scrolls to, flashing the whole line. */
    fun reveal(): NavReveal = NavReveal(repo, revision, path, line, column, side)
}
