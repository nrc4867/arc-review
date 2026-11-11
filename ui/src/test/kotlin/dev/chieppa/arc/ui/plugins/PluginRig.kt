package dev.chieppa.arc.ui.plugins

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manager.PluginManager
import dev.chieppa.arc.plugins.manager.PluginPreparer
import dev.chieppa.arc.plugins.manager.PluginServices
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.MountParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.secrets.MemorySecretStore
import dev.chieppa.arc.plugins.secrets.SecretStore
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.util.ArcDispatchers
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

/**
 * One plugin, `todos`, in a temporary folder with the capabilities [capabilitiesJson] declares in its manifest, run in
 * the window by a [FakePluginProcess] that contributes [contributions] and draws views as [draw] says, enabled with
 * [approved].
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class PluginRig(capabilitiesJson: String, contributions: Contributions, val approved: Capabilities, draw: (MountParams) -> String) {
    val dir: Path = createTempDirectory("arc-plugins-ui")
    val folder: Path = dir.resolve(ID).also {
        Files.createDirectories(it)
        Files.writeString(it.resolve("plugin.json"), """{"id":"$ID","name":"TODOs","version":"0.1.0","apiVersion":1,"entry":"src/index.tsx","capabilities":$capabilitiesJson}""")
    }
    val process = FakePluginProcess(contributions, draw)
    val console = FakeConsole()
    val ready = PluginPreparer { f, root, manifest, settings ->
        Preparation.Ready(WorkerSpec(manifest.id, root), InitializeParams(1, PluginInfo(manifest.id, manifest.version, f.toString()), "src/index.tsx", settings))
    }
    val enabled = PluginsConfig(sources = listOf(PluginSourceSpec.Directory(folder.toString())), enabled = setOf(ID), approved = mapOf(ID to approved))
    private var scope: CoroutineScope? = null

    /** Runs [body], then ends the plugins' work. */
    fun test(body: ComposeUiTest.() -> Unit) = runComposeUiTest {
        try {
            body()
        } finally {
            scope?.cancel()
        }
    }

    /** The window with the plugins [config] sets up, prepared by [preparer], their secrets kept in [secrets]. */
    fun open(
        test: ComposeUiTest,
        preparer: PluginPreparer = ready,
        config: PluginsConfig = enabled,
        secrets: SecretStore = MemorySecretStore(lasting = true),
    ): Workbench {
        val dispatcher = UnconfinedTestDispatcher(test.mainClock.scheduler)
        // The plugins' timers (heartbeat, restarts) wait on a clock of their own that the test never advances.
        val scope = CoroutineScope(UnconfinedTestDispatcher(TestCoroutineScheduler())).also { scope = it }
        val storage = PluginStorage.InMemory(config)
        val workbench = Workbench(
            dispatchers = ArcDispatchers.on(dispatcher),
            plugins = { ui ->
                val grep = GitGrep({ _, _ -> CommandResult(1, "", "") })
                PluginManager(PluginServices(scope, process, storage, ConsoleProcesses(console, scope), grep, preparer, ui = ui, secrets = secrets))
            },
        )
        test.setStretchContent(StretchThemes.WorkshopDark) { ArcFrame(workbench) }
        test.waitForIdle()
        return workbench
    }

    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    companion object {
        const val ID = "todos"

        fun ComposeUiTest.shows(text: String): Boolean = onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()

        fun ComposeUiTest.showsTag(tag: String): Boolean = onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
    }
}
