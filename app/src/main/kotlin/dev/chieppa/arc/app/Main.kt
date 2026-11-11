package dev.chieppa.arc.app

import androidx.compose.ui.window.application
import dev.chieppa.stretch.devtools.DevTools
import dev.chieppa.stretch.document.appDataDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import dev.chieppa.arc.core.watch.WatchSeeds
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_mine
import dev.chieppa.arc.design.generated.filter_review_requested
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.design.generated.runs_seed_mine
import dev.chieppa.arc.design.generated.runs_seed_waiting
import dev.chieppa.stretch.frame.DesktopNotifications
import org.jetbrains.compose.resources.getString
import java.nio.file.Path
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (CommandLine.handles(args)) {
        val code = runBlocking { CommandLine(appDataDirectory(AppConstants.APP_ID), Path.of("").toAbsolutePath(), System.out, System.err).run(args.toList()) }
        exitProcess(code)
    }
    DevTools.startIfRequested()
    val session = AppSession(appDataDirectory(AppConstants.APP_ID), Conversations.load())
    val initial = session.settings.toPreferences()
    val runWatch = RunWatch(
        session.runWatchlist { runBlocking { RunWatchlist.seeds(getString(Res.string.runs_seed_mine), getString(Res.string.runs_seed_waiting)) } },
        session.timings(),
    )
    val watch = PullWatch(session.watchlist { runBlocking { WatchSeeds.create(getString(Res.string.filter_review_requested), getString(Res.string.filter_mine)) } })
    application {
        ArcWindow(
            session = session,
            initial = initial,
            watch = watch,
            runWatch = runWatch,
            notifier = { title, text -> DesktopNotifications.show(title, text) },
            onPreferencesChange = { look -> withContext(Dispatchers.IO) { session.saveSettings { look.toSettings(it) } } },
            onExit = {
                exitStep { session.saveWatchlist(watch.watchlist) }
                exitStep { session.saveRunWatchlist(runWatch.watchlist) }
                exitStep { session.saveTimings(runWatch.timings) }
                exitStep { DesktopNotifications.close() }
                exitStep { session.close() }
                exitApplication()
            },
        )
    }
}
