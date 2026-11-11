package dev.chieppa.arc.app

import dev.chieppa.arc.plugins.bundle.EsbuildBundler
import dev.chieppa.arc.plugins.bundle.ProcessCommandRunner
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.PluginHostScript
import dev.chieppa.arc.plugins.host.ProcessLauncher
import dev.chieppa.arc.plugins.manager.NodePluginPreparer
import dev.chieppa.arc.plugins.manager.PluginManager
import dev.chieppa.arc.plugins.manager.PluginServices
import dev.chieppa.arc.plugins.manager.PluginUi
import dev.chieppa.arc.plugins.runtime.HttpDownloader
import dev.chieppa.arc.plugins.runtime.PluginTools
import dev.chieppa.arc.plugins.runtime.ToolInstaller
import dev.chieppa.arc.plugins.runtime.ToolPins
import dev.chieppa.arc.plugins.source.NioDirectoryWatcher
import dev.chieppa.arc.store.plugins.PluginFiles
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

/**
 * Plugins as the app runs them, from the data folder [dataDir]: their setup and stores, the pinned Node and esbuild
 * installed under `runtimes`, bundles in `plugin-cache` and the host script in `plugin-runtime`, all on [scope].
 */
internal object PluginSetup {
    fun manager(dataDir: Path, scope: CoroutineScope, ui: PluginUi): PluginManager {
        val tools = PluginTools(ToolInstaller(dataDir.resolve(AppConstants.RUNTIMES_FOLDER), HttpDownloader), ToolPins.bundled(), ProcessCommandRunner)
        val preparer = NodePluginPreparer(
            tools,
            EsbuildBundler(tools::esbuild),
            PluginHostScript(dataDir.resolve(AppConstants.PLUGIN_RUNTIME_FOLDER))::file,
            dataDir.resolve(AppConstants.PLUGIN_CACHE_FOLDER),
        )
        val services = PluginServices(
            scope = scope,
            launcher = ProcessLauncher(scope),
            preparer = preparer,
            storage = PluginFiles(dataDir.resolve(AppConstants.PLUGINS_FILE), dataDir.resolve(AppConstants.PLUGIN_STORE_FOLDER)),
            consoles = ConsoleProcesses(ProcessLauncher(scope), scope),
            grep = GitGrep(ProcessCommandRunner),
            ui = ui,
            watcher = NioDirectoryWatcher,
            nodeVersion = tools.pinnedNode,
            nodeReady = { !tools.needsDownload() },
        )
        return PluginManager(services)
    }
}
