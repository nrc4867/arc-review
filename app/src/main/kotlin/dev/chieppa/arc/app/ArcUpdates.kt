package dev.chieppa.arc.app

import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.core.activity.track
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.activity_update_check
import dev.chieppa.arc.design.generated.activity_update_download
import dev.chieppa.arc.host.github.releases.ReleaseChecker
import dev.chieppa.arc.host.github.releases.ReleaseOffer
import dev.chieppa.arc.install.AtomicFiles
import dev.chieppa.arc.install.InstallConstants
import dev.chieppa.arc.install.InstalledVersion
import dev.chieppa.arc.install.ReleaseTarget
import dev.chieppa.arc.tools.DownloadProgress
import dev.chieppa.arc.ui.update.AppUpdates
import org.jetbrains.compose.resources.getString
import java.io.IOException

/**
 * The installed Arc updating itself: releases from [releases] for this machine's [target], downloaded into the data
 * folder, checked, and staged by the [installation]'s updater; activating one or switching back sets [restartPending],
 * so Arc starts again through its launcher once the window has closed. Checks and downloads report to [activity].
 */
class ArcUpdates(
    private val installation: Installation,
    private val releases: ReleaseChecker,
    private val target: String,
    override val version: String,
    private val activity: ActivityReporter = ActivityReporter.NONE,
) : AppUpdates {
    @Volatile
    private var offer: ReleaseOffer? = null

    @Volatile
    private var staged: InstalledVersion? = null

    /** Whether Arc should start again through its launcher once the window closes. */
    @Volatile
    var restartPending = false
        private set

    override val previous: String? get() = installation.state().previous?.version

    override suspend fun check(): String? = activity.track(ActivityKind.Update, getString(Res.string.activity_update_check)) {
        releases.newer(version) { ReleaseTarget.jarName(target, it) }.also { offer = it }?.version
    }

    override suspend fun prepare(progress: (Long, Long?) -> Unit) {
        val offer = offer ?: throw IOException("No release to update to")
        val folder = installation.layout.app.resolve(InstallConstants.PARTIAL_PREFIX + DOWNLOADS)
        val title = getString(Res.string.activity_update_download, offer.version)
        try {
            val jar = activity.track(ActivityKind.Update, title) { releases.download(offer, folder, DownloadProgress { done, total -> progress(done, total) }) }
            staged = installation.updater.stage(jar)
        } finally {
            AtomicFiles.deleteTree(folder)
        }
    }

    override suspend fun activate() {
        installation.updater.activate(staged ?: throw IOException("No update is ready"))
        restartPending = true
    }

    override suspend fun switchBack() {
        installation.updater.switchBack() ?: throw IOException("No previous version")
        restartPending = true
    }

    private companion object {
        const val DOWNLOADS = "downloads"
    }
}
