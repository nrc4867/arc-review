package dev.chieppa.arc.ui.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.host.github.pulls.PullRequestSummary
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.host.conversation.NoConversation
import dev.chieppa.arc.host.conversation.ReviewConversation
import dev.chieppa.arc.ui.actions.registerArcActions
import dev.chieppa.arc.ui.actions.registerContextActions
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
import dev.chieppa.arc.ui.services.SystemClipboard
import dev.chieppa.arc.ui.services.TextClipboard
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.Keymap
import dev.chieppa.stretch.frame.FrameLayout
import dev.chieppa.stretch.frame.PaneDropTarget
import dev.chieppa.stretch.frame.PaneItemDrop
import dev.chieppa.stretch.frame.PaneLayout
import dev.chieppa.stretch.frame.WindowBounds
import dev.chieppa.stretch.ui.TreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The main window's state: the look the person chose, where the drawers are, the open review and its panes, which
 * overlays are open, and the actions that change them. Views read it and recompose when it changes. Sources are read
 * through [data] on [scope]; threads and staged comments come from [conversation]. [reviews] are the saved reviews the
 * top bar offers, and [onOpenReview] opens one by id, or a new one for null. [onQuit] closes the app. Copied paths and
 * refs go to [clipboard]. [watch] holds the saved filters and notification rules, which every review shares.
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
    val clipboard: TextClipboard = SystemClipboard,
    val watch: PullWatch = PullWatch(),
) {
    var preferences by mutableStateOf(preferences)
    var frameLayout by mutableStateOf(defaultFrameLayout())
    var paletteOpen by mutableStateOf(false)
    var settingsOpen by mutableStateOf(false)
    var finderOpen by mutableStateOf(false)
    var addSourceOpen by mutableStateOf(false)

    /** What the tree's files drag into the pane grid through. */
    val dropTarget = PaneDropTarget()

    /** Works out where a pane opened in a window of its own goes, once the window is on screen; null where panes cannot open windows. */
    var windowBounds: (() -> WindowBounds)? = null

    /** Where a pane opened in a window of its own goes, over the viewport; null where panes cannot open windows. */
    val newWindowBounds: WindowBounds? get() = windowBounds?.invoke()

    /** What the context menu item being checked or run acts on; null for the palette and keys. */
    var menuTarget: MenuTarget? = null
        private set

    /** Runs [block] with [target] as what actions act on, as a context menu item does. */
    fun <T> withTarget(target: MenuTarget?, block: () -> T): T {
        val before = menuTarget
        menuTarget = target
        try {
            return block()
        } finally {
            menuTarget = before
        }
    }

    /** Whether the Staged comments drawer has been opened for the staged comments it holds. */
    var stagedShown = false

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

    /** The GitHub repo each of the review's repos was cloned from, for those cloned from GitHub. */
    var gitHubRepos by mutableStateOf<Map<RepoName, GitHubRepo>>(emptyMap())

    /** What each pane on screen offers the window's commands, by pane id. */
    val paneCommands = mutableStateMapOf<String, PaneCommands>()

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
        registerContextActions(registry, this)
    }

    /** Opens the Pull requests drawer and counts every notification as seen. */
    fun openPullRequests() {
        watch.unseen = 0
        if (frameLayout.drawers.firstOrNull { it.id == DrawerIds.PULL_REQUESTS }?.open != true) toggleDrawer(DrawerIds.PULL_REQUESTS)
    }

    /** The source [pull] is in a local clone of its repo, or null when no repo here was cloned from it. */
    fun sourceOf(pull: PullRequestSummary): Source.PullRequest? =
        gitHubRepos.entries.firstOrNull { it.value == pull.repo }?.let { Source.PullRequest(it.key, pull.number) }

    /** The name of the review that holds [source]: the open one, else the first saved one with it. */
    fun reviewHolding(source: Source): String? {
        if (source in state.review.sources) return state.review.name
        return reviews.firstOrNull { it.id != state.review.id.value && source.ref in it.sources }?.name
    }

    /** Removes saved filter [id], with its rules and its tab in this review. */
    fun removeFilter(id: String) {
        state.review.pages.filter { it is Page.Filter && it.filter == id }.forEach { panes.close(it.id.value) }
        watch.removeFilter(id)
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

    /**
     * Takes [source] out of the review, closing its unpinned panes and its open tree rows; its marks stay, so adding it
     * again restores them.
     */
    fun removeSource(source: Source) {
        sources.removeSource(source)
        treeState.expanded = treeState.expanded.filterNot { it is String && TreeKeys.belongsTo(it, source) }.toSet()
        treeState.selection = treeState.selection.filterNot { it is String && TreeKeys.belongsTo(it, source) }.toSet()
        state.active?.let(::activateSource)
    }

    /** Whether the pane [id] wraps long lines: as it was set, or as the settings say. */
    fun wrap(id: String): Boolean = state.paneState(id).wrap ?: preferences.wrapLines

    /**
     * Opens the file the tree row [key] stands for where it was dropped on the grid. A file of another source opens in
     * that source's own layout, in a new tab, since the drop was aimed at the layout being left.
     */
    fun dropFile(key: String, drop: PaneItemDrop) {
        val (ref, path) = TreeKeys.fileOf(key) ?: return
        val source = state.review.sources.firstOrNull { it.ref == ref } ?: return
        if (source == state.active || drop is PaneItemDrop.InWindow) panes.openChangeAt(source, path, drop) else panes.openChange(source, path, newTab = true)
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
