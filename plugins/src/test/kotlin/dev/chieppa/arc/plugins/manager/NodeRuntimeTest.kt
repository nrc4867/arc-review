package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.bundle.CommandResult
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.PluginHostScript
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.ProcessLauncher
import dev.chieppa.arc.plugins.manifest.ApiVersion
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.runtime.PermissionFlags
import dev.chieppa.arc.plugins.view.Props
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Tag
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bundled plugin host in a real Node, named by the `arc.plugins.node` system property: a plugin loads, draws a
 * panel through the React reconciler and redraws when its button's event comes back. Run with `./gradlew nodeTest`.
 */
@Tag("node")
class NodeHostTest {
    private val dir = createTempDirectory("arc-node-host")
    private val folder = dir.resolve("counter").also { Files.createDirectories(it) }
    private val bundle = folder.resolve("bundle.js").also {
        Files.writeString(
            it,
            """
            const React = require("react");
            const { Column, Button, Text } = require("@review/plugin");
            function Counter() {
              const [n, setN] = React.useState(0);
              return React.createElement(Column, null, React.createElement(Text, null, "Count " + n), React.createElement(Button, { label: "Add", onClick: () => setN(n + 1) }));
            }
            module.exports = { default: { panels: [{ id: "counter", title: "Counter", render: () => React.createElement(Counter) }] } };
            """.trimIndent(),
        )
    }

    @Test
    fun aPanelDrawsAndRedrawsAfterAnEvent() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val node = System.getProperty("arc.plugins.node")
            val script = PluginHostScript(dir.resolve("runtime")).file()
            val manifestText = """{"id":"counter","version":"1","apiVersion":1,"entry":"bundle.js","capabilities":{"panels":true}}"""
            Files.writeString(folder.resolve("plugin.json"), manifestText)
            val preparer = PluginPreparer { f, manifest, settings ->
                val flags = PermissionFlags.of(manifest.capabilities, f, listOf(script), 0)
                Preparation.Ready(
                    LaunchSpec(listOf(node) + flags + script.toString(), f),
                    InitializeParams(ApiVersion.CURRENT, PluginInfo(manifest.id, manifest.version, f.toString()), bundle.toString(), settings),
                )
            }
            val services = PluginServices(
                scope, ProcessLauncher(scope), preparer, PluginStorage.InMemory(PluginsConfig(enabled = setOf("counter"))),
                ConsoleProcesses(ProcessLauncher(scope), scope), GitGrep({ _, _ -> CommandResult(1, "", "") }),
            )
            val manager = PluginManager(services).apply { start(); addDirectory(folder) }
            val instance = manager.instance("counter")!!
            withTimeout(TIMEOUT) { instance.status.first { it is PluginStatus.Running } }
            val view = instance.mount(ViewKind.Panel, "counter", CallContext())
            val drawn = withTimeout(TIMEOUT) { view.nodes.first { it.isNotEmpty() } }.single()
            assertEquals("Count 0", drawn.children[0].children.joinToString("") { it.text.orEmpty() })
            val button = drawn.children[1]
            assertEquals("Add", Props.string(button.props, "label"))
            view.event(button.id, "onClick")
            withTimeout(TIMEOUT) { view.nodes.first { nodes -> nodes.single().children[0].children.joinToString("") { it.text.orEmpty() } == "Count 1" } }
            manager.stop()
        } finally {
            scope.cancel()
            dir.toFile().deleteRecursively()
        }
    }

    private companion object {
        const val TIMEOUT = 20_000L
    }
}
