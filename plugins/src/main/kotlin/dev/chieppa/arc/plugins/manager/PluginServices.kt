package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.HostTimings
import dev.chieppa.arc.plugins.host.WorkerLauncher
import dev.chieppa.arc.plugins.net.HttpFetcher
import dev.chieppa.arc.plugins.net.JdkHttpFetcher
import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import dev.chieppa.arc.plugins.source.DirectoryWatcher
import kotlinx.coroutines.CoroutineScope

/**
 * What running plugins need, shared by all of them: the [scope] their work runs on, how their workers start
 * ([workers], prepared by [preparer]) and are timed ([timings], [clock]), where their setup and stores are kept
 * ([storage]) with their secret settings ([secrets]), the processes they run ([consoles]), git searches ([grep]), HTTP requests ([fetcher]), the window
 * ([ui]), and the [watcher] of their folders, whose changes reload them after [reloadMillis] of quiet. [nodeVersion]
 * is the Node plugins run on, and [nodeReady] whether it is on this machine already; [shutdown] ends what runs them
 * once every plugin has stopped.
 */
class PluginServices(
    val scope: CoroutineScope,
    val workers: WorkerLauncher,
    val storage: PluginStorage,
    val consoles: ConsoleProcesses,
    val grep: GitGrep,
    val preparer: PluginPreparer = EntryPreparer,
    val fetcher: HttpFetcher = JdkHttpFetcher,
    val ui: PluginUi = PluginUi.NONE,
    val watcher: DirectoryWatcher = DirectoryWatcher.NONE,
    val timings: HostTimings = HostTimings(),
    val clock: () -> Long = System::currentTimeMillis,
    val reloadMillis: Long = RELOAD_MILLIS,
    val nodeVersion: String? = null,
    val nodeReady: () -> Boolean = { true },
    val shutdown: () -> Unit = {},
    val secrets: SecretStore = MemorySecretStore(),
) {
    private companion object {
        const val RELOAD_MILLIS = 300L
    }
}
