package dev.chieppa.arc.ui.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import dev.chieppa.arc.ui.actions.registerPullActions
import dev.chieppa.arc.ui.actions.registerReviewActions
import dev.chieppa.arc.ui.drawers.defaultFrameLayout
import dev.chieppa.arc.ui.conversation.CommentTools
import dev.chieppa.arc.ui.conversation.ConversationViews
import dev.chieppa.arc.ui.conversation.ThreadViews
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.arc.ui.services.NoConversations
import dev.chieppa.arc.ui.panes.PaneCommands
import dev.chieppa.arc.ui.panes.PaneTexts
import dev.chieppa.arc.ui.review.MarkController
import dev.chieppa.arc.ui.review.PaneController
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.review.SourceController
import dev.chieppa.arc.ui.review.SourceLoad
import dev.chieppa.arc.ui.switcher.FollowedLookup
import dev.chieppa.arc.ui.switcher.SwitcherState
import dev.chieppa.arc.ui.revisions.RevisionController
import dev.chieppa.arc.ui.services.NoRepos
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.services.SystemClipboard
import dev.chieppa.arc.ui.services.LinkOpener
import dev.chieppa.arc.ui.services.SystemBrowser
import dev.chieppa.arc.ui.services.TextClipboard
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.tree.TreeKeys
import dev.chieppa.arc.ui.util.ArcDispatchers
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.Keymap
import dev.chieppa.stretch.frame.FrameLayout
import dev.chieppa.stretch.frame.PaneDropTarget
import dev.chieppa.stretch.frame.PaneItemDrop
import dev.chieppa.stretch.frame.PaneLayout
import dev.chieppa.stretch.frame.WindowBounds
import dev.chieppa.stretch.ui.TreeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The main window's state: the look the person chose, where the drawers are, the workspace and its panes, which
 * overlays are open, and the actions that change them. Views read it and recompose when it changes. Sources are read
 * through [data] on [scope]; the host's threads and the person's own comments go through [comments], from
 * [conversationService] and [localConversation], with sample threads from [conversation]. [onQuit] closes the app. Copied
 * paths and refs go to [clipboard], and web links open through [links]. [watch] holds the saved filters and
 * notification rules. All of the window's work runs on [dispatchers], [scope] on their main.
 */
class Workbench(
    preferences: UiPreferences = UiPreferences(),
    onQuit: () -> Unit = {},
    val data: ReviewData = NoRepos,
    val state: WorkspaceState = WorkspaceState(),
    val conversation: ReviewConversation = NoConversation,
    val dispatchers: ArcDispatchers = ArcDispatchers.Real,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.main),
    val clipboard: TextClipboard = SystemClipboard,
    val watch: PullWatch = PullWatch(),
    val links: LinkOpener = SystemBrowser,
    conversationService: ConversationService = NoConversations,
    localConversation: LocalConversation = LocalConversation(),
) {
    var preferences by mutableStateOf(preferences)
    var frameLayout by mutableStateOf(defaultFrameLayout())
    var paletteOpen by mutableStateOf(false)
    var settingsOpen by mutableStateOf(false)
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

    /** Whether the file tree has the keyboard, so the marking keys act on its selection. */
    var treeFocused by mutableStateOf(false)

    val treeState = TreeState(expanded = state.views.values.flatMap { it.expanded }.toSet() + listOfNotNull(state.active?.let(TreeKeys::source)))
    val sources = SourceController(state, data, scope)
    val panes = PaneController(state)
    val marks = MarkController(state, panes)
    val revisions = RevisionController(state, data, scope, panes)

    /** The switcher over the window, and what followed repos hold, asked as it searches. */
    val switcher = SwitcherState()
    val followed = FollowedLookup(data)

    val texts = PaneTexts(data, dispatchers.default)
    val threadViews = ThreadViews()
    val conversationViews = ConversationViews()

    /** The review's conversation: threads, the person's drafts, notes and staged comments, and pushing them. */
    val comments = CommentTools(state, data, conversationService, scope, dispatchers, localConversation)

    /** The GitHub repo each of the workspace's repos was cloned from, for those cloned from GitHub. */
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
        registerPullActions(registry, this)
    }

    /** Opens the Pull requests drawer and counts every notification as seen. */
    fun openPullRequests() {
        watch.unseen = 0
        if (frameLayout.drawers.firstOrNull { it.id == DrawerIds.PULL_REQUESTS }?.open != true) toggleDrawer(DrawerIds.PULL_REQUESTS)
    }

    /** The source [pull] is in a local clone of its repo, or null when no repo here was cloned from it. */
    fun sourceOf(pull: PullRequestSummary): Source.PullRequest? =
        gitHubRepos.entries.firstOrNull { it.value == pull.repo }?.let { Source.PullRequest(it.key, pull.number) }

    /** The GitHub repos being cloned for a pull request, and those whose clone failed. */
    var cloning by mutableStateOf(emptySet<GitHubRepo>())
    var cloneFailed by mutableStateOf(emptySet<GitHubRepo>())

    /**
     * Runs [then] with [pull] as a source here: in the clone of its repo that is known, or else in the app's own clone,
     * made first and added to the repos. Nothing runs while its repo is being cloned or when it cannot be cloned.
     */
    fun withPullSource(pull: PullRequestSummary, then: (Source.PullRequest) -> Unit) {
        sourceOf(pull)?.let { return then(it) }
        if (pull.repo in cloning) return
        scope.launch {
            cloning = cloning + pull.repo
            cloneFailed = cloneFailed - pull.repo
            try {
                val name = data.clone(pull.repo)?.let { sources.addRepo(it) }
                if (name == null) {
                    cloneFailed = cloneFailed + pull.repo
                } else {
                    gitHubRepos = gitHubRepos + (name to pull.repo)
                    then(Source.PullRequest(name, pull.number))
                }
            } finally {
                cloning = cloning - pull.repo
            }
        }
    }

    /** Makes [source] active, opening it in the strip first when it is not there; false when its repo is unknown. */
    fun openSource(source: Source): Boolean {
        if (source in state.workspace.sources) {
            activateSource(source)
            return true
        }
        return addSource(source.ref)
    }

    /** Makes [source] active and shows its revision matrix, opening it when it is not open. */
    fun openRevisions(source: Source) {
        if (source != state.active) activateSource(source)
        panes.openRevisions(source)
    }

    /** Removes saved filter [id], with its rules and its tab. */
    fun removeFilter(id: String) {
        state.workspace.pages.filter { it is Page.Filter && it.filter == id }.forEach { panes.close(it.id.value) }
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
        val otherSources = state.workspace.sources.filter { it != source }.map(TreeKeys::source).toSet()
        treeState.expanded = treeState.expanded - otherSources + TreeKeys.source(source)
    }

    /** Opens the source [ref] names in the strip and makes it active; false when it names none or its repo is unknown. */
    fun addSource(ref: String): Boolean {
        if (!sources.addSource(ref)) return false
        state.active?.let(::activateSource)
        return true
    }

    /** Opens the source [ref] names in the strip without leaving the active one; false when it names none or its repo is unknown. */
    fun addSourceBeside(ref: String): Boolean {
        val before = state.active
        if (!sources.addSource(ref)) return false
        (before ?: state.active)?.let(::activateSource)
        return true
    }

    /** Opens [source]'s first changed file in a new pane once the source is read; nothing when it cannot be read. */
    fun openFirstChange(source: Source) {
        scope.launch {
            val load = snapshotFlow { state.loads[source] }.first { it is SourceLoad.Ready || it is SourceLoad.Failed }
            load?.ready?.files?.firstOrNull()?.let { panes.openChange(source, it.path, newTab = true) }
        }
    }

    /**
     * Takes [source] out of the strip, closing its unpinned panes and its open tree rows; its marks stay, so opening it
     * again restores them.
     */
    fun removeSource(source: Source) {
        sources.removeSource(source)
        treeState.expanded = treeState.expanded.filterNot { it is String && TreeKeys.belongsTo(it, source) }.toSet()
        treeState.selection = treeState.selection.filterNot { it is String && TreeKeys.belongsTo(it, source) }.toSet()
        state.active?.let(::activateSource)
    }

    /** The pull requests in the strip that GitHub says are merged or closed. */
    val endedPulls: List<Source> get() = state.workspace.sources.filter { state.pulls[it]?.state?.ended == true }

    /** Takes every merged and closed pull request out of the strip; their marks stay. */
    fun closeEndedPulls() {
        endedPulls.forEach(::removeSource)
    }

    /** Whether the pane [id] wraps long lines: as it was set, or as the settings say. */
    fun wrap(id: String): Boolean = state.paneState(id).wrap ?: preferences.wrapLines

    /**
     * Opens the file the tree row [key] stands for where it was dropped on the grid. A file of another source opens in
     * that source's own layout, in a new tab, since the drop was aimed at the layout being left.
     */
    fun dropFile(key: String, drop: PaneItemDrop) {
        val (ref, path) = TreeKeys.fileOf(key) ?: return
        val source = state.workspace.sources.firstOrNull { it.ref == ref } ?: return
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
