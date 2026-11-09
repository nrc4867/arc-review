package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.ui.pulls.PullPoller
import dev.chieppa.arc.ui.runs.RunSearches
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchNotifier
import dev.chieppa.arc.ui.services.DesktopNotifier
import dev.chieppa.arc.ui.util.ArcDispatchers

/**
 * Polls [github] for [watch] while the window is open, with the pull request searches the Mine run rules need, telling
 * the person of what a poll sets off through [notifier].
 */
@Composable
fun WatchPolling(watch: PullWatch, github: GitHub, notifier: DesktopNotifier, dispatchers: ArcDispatchers) {
    LaunchedEffect(watch) { PullPoller(watch, github, WatchNotifier(watch, notifier), dispatchers, RunSearches.ALL).run() }
}
