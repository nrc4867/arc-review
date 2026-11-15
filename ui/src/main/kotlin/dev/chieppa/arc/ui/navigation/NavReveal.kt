package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.source.RepoName

/**
 * A place a jump goes to, for the pane showing [path] of [repo] at [revision] to scroll to, put its caret on and flash:
 * [line] on [side] from [column] until [end], or the whole line when [end] is null.
 */
data class NavReveal(
    val repo: RepoName,
    val revision: String,
    val path: String,
    val line: Int,
    val column: Int,
    val side: LineSide = LineSide.New,
    val end: Int? = null,
)
