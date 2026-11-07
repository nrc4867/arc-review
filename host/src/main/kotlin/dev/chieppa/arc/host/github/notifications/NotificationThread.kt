package dev.chieppa.arc.host.github.notifications

import dev.chieppa.arc.host.github.repo.GitHubRepo
import java.time.Instant

/**
 * One of the person's GitHub notification threads: why it reached them ([reason], such as `author` or `mention`), what
 * it is about ([type], such as `PullRequest`, with its [title] and [number] when the subject has one) and when it last
 * changed.
 */
data class NotificationThread(
    val id: String,
    val reason: String,
    val unread: Boolean,
    val updatedAt: Instant,
    val title: String,
    val type: String,
    val repo: GitHubRepo?,
    val number: Int?,
) {
    companion object {
        const val PULL_REQUEST = "PullRequest"
        const val AUTHOR = "author"
        const val MENTION = "mention"
        const val TEAM_MENTION = "team_mention"
    }
}
