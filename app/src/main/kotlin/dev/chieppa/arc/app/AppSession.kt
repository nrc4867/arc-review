package dev.chieppa.arc.app

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.host.conversation.ReviewConversation
import dev.chieppa.arc.store.repos.RepoRegistryFile
import dev.chieppa.arc.store.review.ReviewMapping
import dev.chieppa.arc.store.review.ReviewStore
import dev.chieppa.arc.store.settings.AppSettings
import dev.chieppa.arc.store.settings.SettingsFile
import dev.chieppa.arc.ui.review.ReviewState
import dev.chieppa.arc.ui.services.GitReviewData
import dev.chieppa.arc.ui.services.ReviewData
import dev.chieppa.arc.ui.workbench.ReviewChoice
import java.io.Closeable
import java.nio.file.Path

/**
 * What the app keeps between runs, in [dataDir]: the settings, every review, and the repos they read from, which all
 * reviews share. Repos are read through git for the review open in [current].
 */
class AppSession(dataDir: Path, val conversation: ReviewConversation) : Closeable {
    private val settingsFile = SettingsFile(dataDir.resolve(AppConstants.SETTINGS_FILE))
    private val reviewStore = ReviewStore(dataDir.resolve(AppConstants.REVIEWS_FOLDER))
    private val registry = RepoRegistryFile(dataDir.resolve(AppConstants.REPOS_FILE))

    var settings: AppSettings = settingsFile.load()
        private set

    @Volatile
    private var repos: Map<RepoName, Path> = registry.load()

    /** The review open in the window, whose repos git reads from. */
    @Volatile
    var current: ReviewState? = null

    private val git = GitRepositories({ name -> (current?.repos ?: repos)[name] })
    val data: ReviewData = GitReviewData(git)

    /** Every saved review, by name. */
    fun reviews(): List<ReviewChoice> = reviewStore.all().map { ReviewChoice(it.id, it.name) }

    /** The review [id], or the last one open, or a new one named [defaultName] when there is none. */
    fun open(id: String?, defaultName: String): ReviewState {
        val stored = (id ?: settings.lastReviewId)?.let(reviewStore::load) ?: reviewStore.all().firstOrNull()
        val state = if (stored != null) {
            ReviewPersistence.toState(stored, repos)
        } else {
            create(defaultName)
        }
        rememberOpen(state.review.id.value)
        current = state
        return state
    }

    /** A new review named [name], saved at once. */
    fun create(name: String): ReviewState {
        val taken = reviewStore.all().map { it.id }.toSet()
        val review = ReviewPersistence.newReview(name, taken)
        reviewStore.save(ReviewMapping.toStored(review))
        return ReviewState(review, repos)
    }

    /** Saves [state], with the tree rows [expanded], and the repos it knows. */
    fun save(state: ReviewState, expanded: Set<Any>) {
        reviewStore.save(ReviewPersistence.toStored(state, expanded))
        if (state.repos != repos) {
            repos = repos + state.repos
            registry.save(repos)
        }
    }

    fun saveSettings(change: (AppSettings) -> AppSettings) {
        settings = change(settings)
        settingsFile.save(settings)
    }

    private fun rememberOpen(id: String) {
        if (settings.lastReviewId != id) saveSettings { it.copy(lastReviewId = id) }
    }

    override fun close() {
        git.close()
    }
}
