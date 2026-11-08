package dev.chieppa.arc.app

import dev.chieppa.arc.codeintel.CodeIntel
import dev.chieppa.arc.index.WorkspaceIndex
import dev.chieppa.arc.index.nav.TreeSitterNavigation
import dev.chieppa.arc.index.store.GitBlobStore
import dev.chieppa.arc.ui.navigation.CodeNavigation
import kotlinx.coroutines.Dispatchers
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.core.watch.Watchlist
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.host.conversation.ReviewConversation
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.core.conversation.LocalConversation
import dev.chieppa.arc.host.github.avatars.AvatarCache
import dev.chieppa.arc.store.conversation.ConversationFile
import dev.chieppa.arc.store.repos.RepoRegistryFile
import dev.chieppa.arc.ui.services.ConversationService
import dev.chieppa.arc.ui.services.GitHubConversations
import dev.chieppa.arc.store.settings.AppSettings
import dev.chieppa.arc.store.settings.SettingsFile
import dev.chieppa.arc.store.watch.WatchlistFile
import dev.chieppa.arc.store.workspace.ReviewFolder
import dev.chieppa.arc.store.workspace.StoredWorkspace
import dev.chieppa.arc.store.workspace.WorkspaceFile
import dev.chieppa.arc.ui.review.WorkspaceState
import dev.chieppa.arc.ui.services.GitHubPulls
import dev.chieppa.arc.ui.services.GitReviewData
import dev.chieppa.arc.ui.services.ReviewData
import java.io.Closeable
import java.nio.file.Path

/**
 * What the app keeps between runs, in [dataDir]: the settings, the workspace, the repos, the saved filters and
 * notification rules, the person's drafts, staged comments and notes, people's pictures, and the app's own clones of
 * GitHub repos. Repos are read through git for the workspace in
 * [current], and pull requests through [github].
 */
class AppSession(dataDir: Path, val conversation: ReviewConversation, val github: GitHub = GitHub()) : Closeable {
    private val settingsFile = SettingsFile(dataDir.resolve(AppConstants.SETTINGS_FILE))
    private val workspaceFile = WorkspaceFile(dataDir.resolve(AppConstants.WORKSPACE_FILE))
    private val reviewFolder = ReviewFolder(dataDir.resolve(AppConstants.REVIEWS_FOLDER))
    private val registry = RepoRegistryFile(dataDir.resolve(AppConstants.REPOS_FILE))
    private val watchlistFile = WatchlistFile(dataDir.resolve(AppConstants.WATCHLIST_FILE))

    var settings: AppSettings = settingsFile.load()
        private set

    @Volatile
    private var repos: Map<RepoName, Path> = registry.load()

    /** The workspace in the window, whose repos git reads from. */
    @Volatile
    var current: WorkspaceState? = null

    private val git = GitRepositories({ name -> (current?.repos ?: repos)[name] })
    val data: ReviewData = GitReviewData(git, GitHubPulls(github), dataDir.resolve(AppConstants.CLONES_FOLDER))

    /** Code navigation: the tree-sitter index of the open sources, read from the same repos, answering by name. */
    val codeNavigation: CodeNavigation = run {
        val blobs = GitBlobStore(git)
        val index = WorkspaceIndex(blobs, Dispatchers.Default)
        CodeNavigation(CodeIntel(), index).also { TreeSitterNavigation(index, blobs).register(it.intel) }
    }

    private val conversationFile = ConversationFile(dataDir.resolve(AppConstants.CONVERSATION_FILE))

    /** The conversation on GitHub, with people's pictures cached in the data folder. */
    val conversations: ConversationService = GitHubConversations(github, AvatarCache(dataDir.resolve(AppConstants.AVATARS_FOLDER)), data::gitHubRepo)

    /** The person's drafts, staged comments, notes and summaries as saved. */
    fun localConversation(): LocalConversation = conversationFile.load()

    fun saveConversation(conversation: LocalConversation) {
        conversationFile.save(conversation)
    }

    /** The saved filters and rules; on a first run, those [seeds] makes, saved at once. */
    fun watchlist(seeds: () -> Watchlist): Watchlist {
        if (watchlistFile.exists()) return watchlistFile.load()
        return seeds().also(watchlistFile::save)
    }

    fun saveWatchlist(watchlist: Watchlist) {
        watchlistFile.save(watchlist)
    }

    /**
     * The workspace as saved. Without a workspace file it starts from the saved review that [AppSettings.lastReviewId]
     * names, else the most recently saved review, else empty, and is saved at once.
     */
    fun open(): WorkspaceState {
        val saved = workspaceFile.load()
        val stored = saved ?: reviewFolder.startFrom(settings.lastReviewId) ?: StoredWorkspace()
        if (saved == null) workspaceFile.save(stored)
        return WorkspacePersistence.toState(stored, repos).also { current = it }
    }

    /** Saves [state], with the tree rows [expanded], and the repos it knows. */
    fun save(state: WorkspaceState, expanded: Set<Any>) {
        workspaceFile.save(WorkspacePersistence.toStored(state, expanded))
        if (state.repos != repos) {
            repos = repos + state.repos
            registry.save(repos)
        }
    }

    fun saveSettings(change: (AppSettings) -> AppSettings) {
        settings = change(settings)
        settingsFile.save(settings)
    }

    override fun close() {
        git.close()
    }
}
