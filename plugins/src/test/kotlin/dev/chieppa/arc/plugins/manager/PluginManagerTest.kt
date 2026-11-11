package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.FakePlugin
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.protocol.ActionContribution
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.Contributions
import dev.chieppa.arc.plugins.protocol.Envelope
import dev.chieppa.arc.plugins.protocol.ErrorCodes
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.Methods
import dev.chieppa.arc.plugins.protocol.PanelContribution
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.source.DirectoryWatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PluginManagerTest {
    private val dir = createTempDirectory("arc-plugins")
    private val folder: Path = dir.resolve("todos").also {
        Files.createDirectories(it)
        Files.writeString(it.resolve("plugin.json"), """{"id":"todos","version":"0.1.0","apiVersion":1,"entry":"index.tsx","capabilities":{"panels":true,"git":true}}""")
    }
    private val contributions = Contributions(actions = listOf(ActionContribution("run", "Run")), panels = listOf(PanelContribution("todos", "TODOs")))
    private val changes = MutableSharedFlow<Unit>()
    private val approved = Capabilities(panels = true, git = true, console = true)
    private val storage = PluginStorage.InMemory(PluginsConfig(enabled = setOf("todos"), approved = mapOf("todos" to approved)))

    @AfterTest
    fun cleanUp() {
        dir.toFile().deleteRecursively()
    }

    private fun TestScope.manager(plugin: FakePlugin, consoles: FakePlugin = FakePlugin(backgroundScope)): PluginManager {
        val preparer = PluginPreparer { f, root, manifest, settings ->
            Preparation.Ready(WorkerSpec(manifest.id, root), InitializeParams(1, PluginInfo(manifest.id, manifest.version, f.toString()), "index.tsx", settings))
        }
        val grep = GitGrep({ _, _ -> CommandResult(0, "src/a.ts:3:// TODO one\n", "") })
        val services = PluginServices(
            backgroundScope, plugin, storage, ConsoleProcesses(consoles, backgroundScope), grep, preparer,
            watcher = DirectoryWatcher { changes }, clock = { testScheduler.currentTime },
        )
        return PluginManager(services).apply {
            start()
            addDirectory(folder)
            runCurrent()
        }
    }

    @Test
    fun startsAnEnabledPluginWithOnlyTheContributionsItDeclares() = runTest {
        val manager = manager(FakePlugin(backgroundScope, contributions))
        val instance = manager.instance("todos")!!
        assertIs<PluginStatus.Running>(instance.status.value)
        assertEquals(listOf("todos"), instance.contributions.value.panels.map { it.id })
        assertEquals(emptyList(), instance.contributions.value.actions)
        manager.setEnabled("todos", false)
        runCurrent()
        assertNull(manager.instance("todos"))
        assertEquals(false, manager.entries.value.single().enabled)
    }

    @Test
    fun itsWorkerReadsFromItsSourcesFolder() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        manager(plugin)
        assertEquals(WorkerSpec("todos", folder.toAbsolutePath().normalize()), plugin.workers.single())
    }

    @Test
    fun anUpdateAskingForMoreWaitsUntilThePersonEnablesItAgain() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val manager = manager(plugin)
        Files.writeString(folder.resolve("plugin.json"), """{"id":"todos","version":"0.2.0","apiVersion":1,"entry":"index.tsx","capabilities":{"panels":true,"git":true,"network":["api.example.com"]}}""")
        changes.emit(Unit)
        advanceTimeBy(1_000)
        val waiting = manager.entries.value.single()
        assertNull(waiting.instance)
        assertEquals(listOf<Capability>(Capability.Network("api.example.com")), waiting.asks)
        assertEquals(false, waiting.on)
        manager.setEnabled("todos", true)
        runCurrent()
        assertIs<PluginStatus.Running>(manager.instance("todos")!!.status.value)
        assertEquals(listOf("api.example.com"), storage.loadConfig().approved.getValue("todos").network)
    }

    @Test
    fun aViewShowsWhatThePluginDrawsAndSendsItsEvents() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        val view = manager(plugin).instance("todos")!!.mount(ViewKind.Panel, "todos", CallContext())
        runCurrent()
        val mount = plugin.received.filterIsInstance<Envelope.Request>().last()
        assertEquals(Methods.VIEW_MOUNT, mount.method)
        val patch = """{"view":"${view.id}","ops":[{"op":"create","id":1,"type":"Button","props":{"label":"Go"}},{"op":"append","parent":0,"child":1}]}"""
        plugin.current.write(Envelope.Notification(Methods.VIEW_PATCH, Json.parseToJsonElement(patch)))
        runCurrent()
        assertEquals("Button", view.nodes.value.single().type)
        view.event(1, "onClick")
        runCurrent()
        assertEquals(Methods.VIEW_EVENT, (plugin.received.last() as Envelope.Notification).method)
        view.close()
        runCurrent()
        assertEquals(Methods.VIEW_UNMOUNT, (plugin.received.last() as Envelope.Notification).method)
    }

    @Test
    fun callsAreCheckedAgainstTheManifest() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        manager(plugin)
        plugin.current.write(Envelope.Request(1, Methods.CONSOLE_RUN, buildJsonObject { put("command", Json.parseToJsonElement("""["ls"]""")) }))
        runCurrent()
        assertEquals(ErrorCodes.DENIED, (plugin.received.last() as Envelope.Response).error?.code)
        val context = """{"pattern":"TODO","context":{"source":{"ref":"api#1","kind":"pull","repo":{"id":"github.com/a/api","name":"api"},"worktree":"${dir.toString().replace("\\", "/")}"}}}"""
        plugin.current.write(Envelope.Request(2, Methods.GIT_GREP, Json.parseToJsonElement(context)))
        runCurrent()
        val hits = (plugin.received.last() as Envelope.Response).result.jsonObject.getValue("hits")
        assertEquals("""[{"path":"src/a.ts","line":3,"text":"// TODO one"}]""", hits.toString())
    }

    @Test
    fun settingsAndTheStoreAreKeptAndAChangeIsSent() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        manager(plugin)
        plugin.current.write(Envelope.Request(1, Methods.SETTINGS_SET, buildJsonObject { put("key", "folder"); put("value", "spec/**") }))
        plugin.current.write(Envelope.Request(2, Methods.STORE_SET, buildJsonObject { put("key", "seen"); put("value", 3) }))
        plugin.current.write(Envelope.Request(3, Methods.STORE_GET, buildJsonObject { put("key", "seen") }))
        runCurrent()
        assertEquals(JsonPrimitive("spec/**"), storage.loadConfig().settings.getValue("todos")["folder"])
        assertTrue(plugin.received.any { it is Envelope.Notification && it.method == Methods.SETTINGS_CHANGED })
        assertEquals(JsonPrimitive(3), plugin.received.filterIsInstance<Envelope.Response>().single { it.id == 3L }.result)
    }

    @Test
    fun aSaveInItsFolderReloadsIt() = runTest {
        val plugin = FakePlugin(backgroundScope, contributions)
        manager(plugin)
        changes.emit(Unit)
        advanceTimeBy(1_000)
        assertEquals(2, plugin.launched.size)
        assertTrue(plugin.launched.first().killed)
    }

    @Test
    fun aConsoleProcessPrintsToThePluginAndTakesInput() = runTest {
        Files.writeString(folder.resolve("plugin.json"), """{"id":"todos","version":"0.1.0","apiVersion":1,"entry":"index.tsx","capabilities":{"console":true}}""")
        val plugin = FakePlugin(backgroundScope, contributions)
        val consoles = FakePlugin(backgroundScope)
        manager(plugin, consoles)
        plugin.current.write(Envelope.Request(1, Methods.CONSOLE_RUN, Json.parseToJsonElement("""{"command":["npm","test"],"cwd":"plugin"}""")))
        runCurrent()
        val handle = (plugin.received.last() as Envelope.Response).result.jsonObject.getValue("handle")
        consoles.current.write(Envelope.Notification("ignored"))
        plugin.current.write(Envelope.Request(2, Methods.CONSOLE_WRITE, buildJsonObject { put("handle", handle); put("text", "y") }))
        runCurrent()
        assertEquals(listOf("y"), consoles.current.raw)
        consoles.current.end(0)
        runCurrent()
        val sent = plugin.received.filterIsInstance<Envelope.Notification>().map { it.method }
        assertTrue(Methods.CONSOLE_OUTPUT in sent && Methods.CONSOLE_EXIT in sent)
    }
}
