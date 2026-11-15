package dev.chieppa.arc.ui.navigation

import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.core.conversation.LineSide
import dev.chieppa.arc.ui.conversation.ThreadViews
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.WorkspaceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The window's navigation history: follows where the person reads, the focused pane's file at its caret or else its
 * top line, and keeps the place they leave whenever another file comes up (a file opened from the tree, the switcher,
 * a list) or a jump goes somewhere (a definition, a usage, the outline, a thread), so [back] and [forward] go
 * between them as a browser does, reopening a file closed since.
 */
class HistoryNavigator(
    private val state: WorkspaceState,
    private val panes: PaneController,
    private val navigation: NavigationController,
    private val threads: ThreadViews,
    private val opener: SymbolOpener,
    private val scope: CoroutineScope,
) {
    val history = NavigationHistory()

    /** Where the person was when last seen. */
    private var here: NavPlace? = null

    /** The jump last seen under way, to tell a new one from it. */
    private var lastJump: Any? = null

    /** The jump [back] or [forward] made, which is no place left. */
    private var ownJump: NavReveal? = null

    /** Follows the person's place for as long as [scope] runs. */
    fun start() {
        scope.launch {
            snapshotFlow { Seen(current(), navigation.reveal ?: threads.reveal) }.collect(::seen)
        }
    }

    /** Where the person is now: the focused pane's file at its caret when the caret is in that file, else at its top line. */
    fun current(): NavPlace? {
        val pane = panes.focused ?: return null
        val id = pane.id.value
        val caret = navigation.caret?.takeIf { it.paneId == id && (it.path == null || it.path == pane.path || it.path == pane.oldPath) }
        val line = caret?.line ?: state.paneLines[id] ?: 1
        return NavPlace(pane.repo, pane.revision.value, pane.path, line, caret?.column ?: 0, caret?.side ?: LineSide.New, pane.base?.value, pane.source)
    }

    /** Goes back to the place left last, if there is one. */
    fun back() {
        history.back(current())?.let(::go)
    }

    /** Goes forward to the place gone back from last, if there is one. */
    fun forward() {
        history.forward(current())?.let(::go)
    }

    private fun go(place: NavPlace) {
        val reveal = opener.open(place)
        ownJump = reveal
        here = place
        navigation.reveal = reveal
    }

    private fun seen(seen: Seen) {
        val (place, jump) = seen
        val before = here
        val jumped = jump != null && jump != lastJump && jump !== ownJump
        lastJump = jump
        if (place != null) here = place
        if (before == null) return
        if (jumped || (place != null && !before.sameFile(place) && jump == null)) history.leave(before)
    }

    /** What the history sees at once: where the person is and the jump under way, if any. */
    private data class Seen(val place: NavPlace?, val jump: Any?)
}
