package dev.chieppa.arc.ui.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.conversation.NoConversation
import dev.chieppa.arc.host.conversation.ReviewConversation
import dev.chieppa.arc.ui.actions.registerArcActions
import dev.chieppa.arc.ui.actions.registerReviewActions
import dev.chieppa.arc.ui.drawers.defaultFrameLayout
import dev.chieppa.arc.ui.conversation.ThreadViews
import dev.chieppa.arc.ui.panes.PaneCommands
import dev.chieppa.arc.ui.panes.PaneTexts
import dev.chieppa.arc.ui.review.MarkController
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.review.SourceController
import dev.chieppa.arc.ui.services.NoRepos
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.Keymap
import dev.chieppa.stretch.frame.FrameLayout
import dev.chieppa.stretch.frame.PaneLayout
import dev.chieppa.stretch.ui.TreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The main window's state: the look the person chose, where the drawers are, the open review and its panes, which
 * overlays are open, and the actions that change them. Views read it and recompose when it changes. Sources are read
 * through [data] on [scope]; threads and staged comments come from [conversation]. [reviews] are the saved reviews the
 * top bar offers, and [onOpenReview] opens one by id, or a new one for null. [onQuit] closes the app.
 */
class Workbench(
    preferences: UiPreferences = UiPreferences(),
    onQuit: () -> Unit = {},
    val data: ReviewData = NoRepos,
    val state: ReviewState = ReviewState(),
    val conversation: ReviewConversation = NoConversation,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    val reviews: List<ReviewChoice> = emptyList(),
    val onOpenReview: (String?) -> Unit = {},
) {
    var preferences by mutableStateOf(preferences)
    var frameLayout by mutableStateOf(defaultFrameLayout())
    var paletteOpen by mutableStateOf(false)
    var settingsOpen by mutableStateOf(false)
    var finderOpen by mutableStateOf(false)
    var addSourceOpen by mutableStateOf(false)

    /** Whether the tree shows only the files changed since they were reviewed. */
    var sinceReview by mutableStateOf(false)

    /** Whether the file tree has the keyboard, so review keys act on its selection. */
    var treeFocused by mutableStateOf(false)

    val treeState = TreeState(expanded = state.views.values.flatMap { it.expanded }.toSet() + listOfNotNull(state.active?.let(TreeKeys::source)))
    val sources = SourceController(state, data, scope)
    val panes = PaneController(state)
    val marks = MarkController(state, panes)

    val texts = PaneTexts(data)
    val threadViews = ThreadViews()

    /** What each pane on screen offers the window's commands, by pane id. */
    val paneCommands = mutableMapOf<String, PaneCommands>()

    /** The active source's panes in the viewport. */
    var paneLayout: PaneLayout
        get() = state.paneLayout
        set(value) {
            state.paneLayout = value
        }

    val registry = ActionRegistry()
    val keymap = Keymap(registry)

    init {
        registerArcActions(registry, this, onQuit)
        registerReviewActions(registry, this)
    }

    fun toggleDrawer(id: String) {
        frameLayout = frameLayout.toggle(id)
    }

    fun resetLayout() {
        frameLayout = defaultFrameLayout()
    }

    /** Makes [source] drive the tree and the viewport: its row opens and the other sources' rows close. */
    fun activateSource(source: Source) {
        sources.activate(source)
        val otherSources = state.review.sources.filter { it != source }.map(TreeKeys::source).toSet()
        treeState.expanded = treeState.expanded - otherSources + TreeKeys.source(source)
    }

    /** Adds the source [ref] names and makes it active; false when it names none or its repo is unknown. */
    fun addSource(ref: String): Boolean {
        if (!sources.addSource(ref)) return false
        state.active?.let(::activateSource)
        return true
    }

    /** Runs [command] on the focused pane, if it is on screen. */
    fun onFocusedPane(command: suspend PaneCommands.() -> Unit) {
        state.paneLayout.activeTab?.let { runOnPane(it, command) }
    }

    /** Runs [command] on the pane [id], if it is on screen. */
    fun runOnPane(id: String, command: suspend PaneCommands.() -> Unit) {
        val commands = paneCommands[id] ?: return
        scope.launch { commands.command() }
    }
}
