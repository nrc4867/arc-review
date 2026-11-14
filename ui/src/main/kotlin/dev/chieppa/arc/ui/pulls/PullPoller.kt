package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Polls GitHub for [watch] while it runs: who the person is, by the token in use, and every saved search in one request
 * every [PullTimes.POLL_MINUTES] minutes, or at once when asked, and the person's notifications as often as GitHub allows. What a poll sets off goes to
 * [notifier]. [extraSearches] are polled in the same request, by key, for others to read from the results. Blocking
 * reads run on [dispatchers]' io.
 */
class PullPoller(
    private val watch: PullWatch,
    private val github: GitHub,
    private val notifier: WatchNotifier,
    private val dispatchers: ArcDispatchers = ArcDispatchers.Real,
    private val extraSearches: Map<String, String> = emptyMap(),
) {
    /** Polls until cancelled. */
    suspend fun run() = coroutineScope {
        launch { searches() }
        launch { notifications() }
    }

    /** One poll of every search, with what it sets off. */
    suspend fun pollOnce() {
        watch.tokenOrigin = withContext(dispatchers.io) { github.tokenOrigin() }
        if (watch.tokenOrigin == null) {
            watch.viewer = null
            watch.status = WatchStatus.Unavailable
            return
        }
        watch.viewer = withContext(dispatchers.io) { github.knownViewer() }
        try {
            watch.viewer = github.viewer()
            val searches = WatchSearches.of(watch.watchlist) + extraSearches
            if (searches.isEmpty()) {
                watch.results = emptyMap()
                watch.status = WatchStatus.Live
                return
            }
            val poll = github.search(searches)
            val now = watch.now()
            val events = WatchEvents.newPulls(watch.watchlist.rules, watch.results, poll.results.mapValues { it.value.pulls })
            // A search GitHub gave up on keeps what it last found.
            watch.results = watch.results.filterKeys { it in poll.failed } + poll.results.mapValues { (_, result) -> FilterResult(result.total, result.pulls, now) }
            poll.budget?.let { watch.budget = it }
            watch.status = WatchStatus.Live
            events.forEach { notifier.tell(it) }
        } catch (e: GitHubException) {
            watch.status = if (e.reason == GitHubException.Reason.Failed) WatchStatus.Failed else WatchStatus.Unavailable
        }
    }

    private suspend fun searches() {
        while (true) {
            pollOnce()
            withTimeoutOrNull(PullTimes.POLL_MILLIS) { watch.refreshRequests.receive() }
        }
    }

    private suspend fun notifications() {
        while (true) {
            try {
                if (withContext(dispatchers.io) { github.hasToken() }) {
                    WatchEvents.threads(watch.watchlist.rules, github.notifications.poll()).forEach { notifier.tell(it) }
                }
            } catch (_: GitHubException) {
            }
            delay(github.notifications.pollSeconds * PullTimes.MILLIS_PER_SECOND)
        }
    }
}
