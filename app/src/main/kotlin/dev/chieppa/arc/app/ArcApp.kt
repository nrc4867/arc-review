package dev.chieppa.arc.app

import androidx.compose.ui.window.application
import dev.chieppa.arc.core.actions.RunWatchlist
import dev.chieppa.arc.core.watch.WatchSeeds
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_mine
import dev.chieppa.arc.design.generated.filter_review_requested
import dev.chieppa.arc.design.generated.runs_seed_mine
import dev.chieppa.arc.design.generated.runs_seed_waiting
import dev.chieppa.arc.install.ReleaseTarget
import dev.chieppa.arc.install.SelfLocation
import dev.chieppa.arc.ui.pulls.PullWatch
import dev.chieppa.arc.ui.runs.RunWatch
import dev.chieppa.stretch.frame.DesktopNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.nio.file.Path

/**
 * Arc's window on the data folder [dataDir]. When this process is the installed Arc ([installation], [self]), it
 * updates itself from GitHub's releases, and after an update or a switch back it starts again through its launcher
 * once the window has closed.
 */
internal fun runArc(dataDir: Path, installation: Installation, self: SelfLocation) {
    val session = AppSession(dataDir, Conversations.load())
    val target = ReleaseTarget.of()
    val version = self.version
    val updates = if (target != null && version != null && installation.isInstalledSelf(self)) {
        ArcUpdates(installation, session.github.releases, target, version)
    } else {
        null
    }
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
                if (updates?.restartPending == true) exitStep { installation.launcher.start() }
                exitApplication()
            },
            updates = updates,
        )
    }
}
