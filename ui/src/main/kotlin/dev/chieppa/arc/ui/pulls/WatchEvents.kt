package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.host.github.notifications.NotificationThread
import dev.chieppa.arc.host.github.pulls.PullRequestSummary

/** Which rules that are on a poll or a batch of notification threads sets off. */
object WatchEvents {
    /**
     * The pull requests each "new PR" rule that is on finds in [after] and did not in [before], by search key. A search
     * with nothing [before] is being polled for the first time, so it sets nothing off. A pull request sets off one
     * notification per poll, for the first rule that finds it.
     */
    fun newPulls(rules: List<NotifyRule>, before: Map<String, FilterResult>, after: Map<String, List<PullRequestSummary>>): List<WatchEvent> {
        val events = mutableListOf<WatchEvent>()
        val told = mutableSetOf<String>()
        for (rule in rules.filter { it.on }) {
            val key = when (rule) {
                is NotifyRule.NewInFilter -> rule.filter
                is NotifyRule.NewLabelled -> rule.id
                else -> continue
            }
            val old = before[key]?.pulls?.map { it.key }?.toSet() ?: continue
            val found = after[key] ?: continue
            found.filter { it.key !in old && told.add(it.key) }.forEach { events += WatchEvent.NewPull(rule, it) }
        }
        return events
    }

    /** The threads that the comment and mention rules that are on match, the first matching rule for each. */
    fun threads(rules: List<NotifyRule>, threads: List<NotificationThread>): List<WatchEvent> {
        val comments = rules.firstOrNull { it is NotifyRule.CommentsOnMyPullRequests && it.on }
        val mentions = rules.firstOrNull { it is NotifyRule.Mentions && it.on }
        return threads.mapNotNull { thread ->
            val rule = when {
                thread.reason in MENTION_REASONS -> mentions
                thread.reason == NotificationThread.AUTHOR && thread.type == NotificationThread.PULL_REQUEST -> comments
                else -> null
            }
            rule?.let { WatchEvent.Thread(it, thread) }
        }
    }

    private val MENTION_REASONS = setOf(NotificationThread.MENTION, NotificationThread.TEAM_MENTION)
}
