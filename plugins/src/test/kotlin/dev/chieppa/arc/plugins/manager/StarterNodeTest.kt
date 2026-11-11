package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.command.ProcessCommandRunner
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.ProcessConsoleLauncher
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.NodeHost
import dev.chieppa.arc.plugins.host.PluginHostScript
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.runtime.NodeLaunch
import dev.chieppa.arc.plugins.runtime.PluginTools
import dev.chieppa.arc.plugins.runtime.ToolInstaller
import dev.chieppa.arc.plugins.runtime.ToolPins
import dev.chieppa.arc.plugins.starter.PluginStarter
import dev.chieppa.arc.plugins.starter.StarterFiles
import dev.chieppa.arc.plugins.typecheck.NodeTypeChecker
import dev.chieppa.arc.plugins.view.Props
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Tag
import java.io.IOException
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A starter plugin as `arc-review plugin new` makes it, in a real Node named by `arc.plugins.node`: it type checks
 * clean against the SDK's types, its panel draws and takes a press, and its action runs. Run with
 * `./gradlew :plugins:nodeTest -Parc.plugins.node=<node>`.
 */
@Tag("node")
class StarterNodeTest {
    private val dir = createTempDirectory("arc-starter-node")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @AfterTest
    fun cleanUp() {
        scope.cancel()
        dir.toFile().deleteRecursively()
    }

    private fun launch(): NodeLaunch {
        val node = Path.of(System.getProperty("arc.plugins.node"))
        val tools = PluginTools(ToolInstaller(dir.resolve("runtimes"), { _, _, _ -> throw IOException("offline") }), ToolPins(Properties()), ProcessCommandRunner, node)
        return NodeLaunch(tools, PluginHostScript(dir.resolve("runtime"))::files, dir.resolve("work"))
    }

    @Test
    fun aStarterTypeChecksCleanDrawsItsPanelAndRunsItsAction() = runBlocking<Unit> {
        val folder = PluginStarter.create(dir.resolve("plugins"), "Hello Starter")
        val launch = launch()
        assertEquals(emptyList(), NodeTypeChecker(launch::checkSpec).check(folder, StarterFiles.ENTRY))

        val host = NodeHost(scope, SystemProcessLauncher(scope), launch::spec)
        val config = PluginsConfig(enabled = setOf(ID), approved = mapOf(ID to Capabilities(panels = true, actions = true)))
        val services = PluginServices(scope, host, PluginStorage.InMemory(config), ConsoleProcesses(ProcessConsoleLauncher(scope), scope), GitGrep({ _, _ -> CommandResult(1, "", "") }), shutdown = host::close)
        val manager = PluginManager(services).apply {
            start()
            addDirectory(folder)
        }
        try {
            val plugin = manager.instance(ID)!!
            withTimeout(TIMEOUT) { plugin.status.first { it is PluginStatus.Running } }
            val view = plugin.mount(ViewKind.Panel, StarterFiles.PANEL, CallContext())
            val drawn = withTimeout(TIMEOUT) { view.nodes.first { it.isNotEmpty() } }
            val button = drawn.single().children.last().children.last()
            assertEquals("Press", Props.string(button.props, "label"))
            view.event(button.id, "onClick")
            withTimeout(TIMEOUT) { plugin.log.lines.first { lines -> lines.any { "pressed 1" in it } } }
            plugin.runAction(StarterFiles.ACTION, CallContext())
            withTimeout(TIMEOUT) { plugin.log.lines.first { lines -> lines.any { "hello from no source" in it } } }
            assertTrue(plugin.status.value is PluginStatus.Running)
        } finally {
            manager.stop()
        }
    }

    private companion object {
        const val ID = "hello-starter"
        const val TIMEOUT = 30_000L
    }
}
