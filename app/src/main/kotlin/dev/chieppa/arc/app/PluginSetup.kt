package dev.chieppa.arc.app

import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.arc.git.process.GitRunner
import dev.chieppa.arc.plugins.command.ProcessCommandRunner
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.ProcessConsoleLauncher
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.gitsource.GitSourceCache
import dev.chieppa.arc.plugins.host.NodeHost
import dev.chieppa.arc.plugins.host.PluginHostScript
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import dev.chieppa.arc.plugins.manager.PluginManager
import dev.chieppa.arc.plugins.manager.PluginServices
import dev.chieppa.arc.plugins.manager.PluginUi
import dev.chieppa.arc.plugins.runtime.NodeLaunch
import dev.chieppa.arc.plugins.runtime.PluginTools
import dev.chieppa.arc.tools.HttpDownloader
import dev.chieppa.arc.tools.ReportingDownloader
import dev.chieppa.arc.tools.ToolInstaller
import dev.chieppa.arc.plugins.secrets.SecretStores
import dev.chieppa.arc.plugins.typecheck.NodeTypeChecker
import dev.chieppa.arc.plugins.source.NioDirectoryWatcher
import dev.chieppa.arc.store.plugins.PluginFiles
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

/**
 * Plugins as the app runs them, from the data folder [dataDir]: their setup and stores, the pinned Node installed under
 * `runtimes`, and the one Node process all plugins run in, on the host files written under `plugin-runtime`, all on
 * [scope], downloads and git reported to [activity], where plugins write their output too. Git sources are fetched with the person's own credentials only.
 */
internal object PluginSetup {
    fun manager(dataDir: Path, scope: CoroutineScope, ui: PluginUi, activity: ActivityLog): PluginManager {
        val tools = PluginTools(ToolInstaller(dataDir.resolve(AppConstants.RUNTIMES_FOLDER), ReportingDownloader(HttpDownloader, activity)), PluginTools.bundledPins(), ProcessCommandRunner)
        val runtime = dataDir.resolve(AppConstants.PLUGIN_RUNTIME_FOLDER)
        val launch = NodeLaunch(tools, PluginHostScript(runtime)::files, runtime.resolve(AppConstants.PLUGIN_WORK_FOLDER))
        val processes = SystemProcessLauncher(scope)
        val host = NodeHost(scope, processes, launch::spec)
        val services = PluginServices(
            scope = scope,
            workers = host,
            storage = PluginFiles(dataDir.resolve(AppConstants.PLUGINS_FILE), dataDir.resolve(AppConstants.PLUGIN_STORE_FOLDER)),
            consoles = ConsoleProcesses(ProcessConsoleLauncher(scope), scope),
            grep = GitGrep(ProcessCommandRunner),
            ui = ui,
            watcher = NioDirectoryWatcher,
            nodeVersion = tools.pinnedNode,
            nodeReady = { !tools.needsDownload() },
            shutdown = host::close,
            secrets = SecretStores.system(),
            typeChecker = NodeTypeChecker(launch::checkSpec),
            gitSources = GitSourceCache(dataDir.resolve(AppConstants.PLUGIN_GIT_FOLDER), GitRunner(activity = activity)),
            output = activity,
        )
        return PluginManager(services)
    }
}
