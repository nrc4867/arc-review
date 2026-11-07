package dev.chieppa.arc.host.github.notifications

import dev.chieppa.arc.host.github.api.GitHubApi
import dev.chieppa.arc.host.github.api.GitHubClient
import dev.chieppa.arc.host.github.api.GitHubException
import java.time.Instant

/**
 * The person's notifications, polled with conditional requests: an answer of 304 costs nothing. The first poll learns
 * what is already there; each later one returns the unread threads that are new or changed since. [pollSeconds] is the
 * wait GitHub asks for between polls.
 */
class NotificationsFeed(private val client: GitHubClient) {
    private var etag: String? = null
    private var lastModified: String? = null
    private var known: Map<String, Instant>? = null

    @Volatile
    var pollSeconds: Long = DEFAULT_POLL_SECONDS
        private set

    suspend fun poll(): List<NotificationThread> {
        val response = client.get(GitHubApi.NOTIFICATIONS, etag, lastModified)
        response.header(GitHubApi.POLL_INTERVAL)?.toLongOrNull()?.let { pollSeconds = maxOf(it, DEFAULT_POLL_SECONDS) }
        if (response.status == GitHubApi.NOT_MODIFIED) return emptyList()
        if (response.status != GitHubApi.OK) throw GitHubException(GitHubException.Reason.Failed, "Notifications answered ${response.status}")
        etag = response.header(GitHubApi.ETAG)
        lastModified = response.header(GitHubApi.LAST_MODIFIED)
        val threads = NotificationsParser.parse(client.parse(response.body))
        val (fresh, now) = changes(known, threads)
        known = now
        return fresh
    }

    companion object {
        const val DEFAULT_POLL_SECONDS = 60L

        /**
         * The unread threads in [threads] that are not in [known] or changed since, and what is known after them. With
         * nothing known yet, nothing is fresh.
         */
        fun changes(known: Map<String, Instant>?, threads: List<NotificationThread>): Pair<List<NotificationThread>, Map<String, Instant>> {
            val now = (known.orEmpty() + threads.associate { it.id to it.updatedAt })
            if (known == null) return emptyList<NotificationThread>() to now
            val fresh = threads.filter { thread -> thread.unread && known[thread.id]?.let { thread.updatedAt.isAfter(it) } != false }
            return fresh to now
        }
    }
}
