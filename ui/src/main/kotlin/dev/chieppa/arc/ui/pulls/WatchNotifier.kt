package dev.chieppa.arc.ui.pulls

import dev.chieppa.arc.core.watch.NotifyRule
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.notify_comment
import dev.chieppa.arc.design.generated.notify_mention
import dev.chieppa.arc.design.generated.notify_new_in_filter
import dev.chieppa.arc.design.generated.notify_new_labelled
import dev.chieppa.arc.design.generated.notify_pull
import dev.chieppa.arc.design.generated.pull_ref
import dev.chieppa.arc.ui.services.DesktopNotifier
import org.jetbrains.compose.resources.getString

/** Tells the person of each [WatchEvent] through [desktop], in the app's words, and counts it on the bell of [watch]. */
class WatchNotifier(private val watch: PullWatch, private val desktop: DesktopNotifier) {
    suspend fun tell(event: WatchEvent) {
        val (title, text) = when (event) {
            is WatchEvent.NewPull -> {
                val pull = event.pull
                val ref = getString(Res.string.pull_ref, pull.repo.name, pull.number)
                val title = when (val rule = event.rule) {
                    is NotifyRule.NewLabelled -> getString(Res.string.notify_new_labelled, rule.label)
                    is NotifyRule.NewInFilter -> getString(Res.string.notify_new_in_filter, watch.watchlist.filter(rule.filter)?.name.orEmpty())
                    else -> return
                }
                title to getString(Res.string.notify_pull, ref, pull.title)
            }
            is WatchEvent.Thread -> {
                val thread = event.thread
                val ref = if (thread.repo != null && thread.number != null) getString(Res.string.pull_ref, thread.repo!!.name, thread.number!!) else thread.repo?.fullName.orEmpty()
                val title = if (event.rule is NotifyRule.Mentions) getString(Res.string.notify_mention, ref) else getString(Res.string.notify_comment, ref)
                title to thread.title
            }
        }
        watch.unseen += 1
        desktop.show(title, text)
    }
}
