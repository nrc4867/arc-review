package dev.chieppa.arc.ui.navigation

import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.core.source.RepoName

/** A place a jump goes to, for the pane showing [path] of [repo] at [revision] to scroll to and put its caret on. */
data class NavReveal(val repo: RepoName, val revision: String, val path: String, val line: Int, val column: Int, val side: LineSide = LineSide.New)
