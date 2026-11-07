package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.host.github.GitHub
import dev.chieppa.arc.ui.pulls.PullPoller
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.pulls.WatchNotifier
import dev.chieppa.arc.ui.services.DesktopNotifier

/** Polls [github] for [watch] while the window is open, telling the person of what a poll sets off through [notifier]. */
@Composable
fun WatchPolling(watch: PullWatch, github: GitHub, notifier: DesktopNotifier) {
    LaunchedEffect(watch) { PullPoller(watch, github, WatchNotifier(watch, notifier)).run() }
}
