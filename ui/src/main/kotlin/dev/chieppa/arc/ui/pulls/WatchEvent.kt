package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.host.github.notifications.NotificationThread
import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** Something a notification rule that is on was waiting for. */
sealed interface WatchEvent {
    val rule: NotifyRule

    /** A pull request a poll found under [rule] that the poll before did not. */
    data class NewPull(override val rule: NotifyRule, val pull: PullRequestSummary) : WatchEvent

    /** A notification thread that matches [rule], such as a comment on the person's pull request. */
    data class Thread(override val rule: NotifyRule, val thread: NotificationThread) : WatchEvent
}
