package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.command.CommandResult
import dev.chieppa.arc.plugins.command.ProcessCommandRunner
import dev.chieppa.arc.plugins.config.PluginStorage
import dev.chieppa.arc.plugins.config.PluginsConfig
import dev.chieppa.arc.plugins.console.ConsoleProcesses
import dev.chieppa.arc.plugins.console.ProcessConsoleLauncher
import dev.chieppa.arc.plugins.git.GitGrep
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.NodeHost
import dev.chieppa.arc.plugins.host.PluginHostScript
import dev.chieppa.arc.plugins.host.PluginStatus
import dev.chieppa.arc.plugins.host.ProcessLauncher
import dev.chieppa.arc.plugins.host.SystemProcessLauncher
import dev.chieppa.arc.plugins.manifest.Capabilities
import dev.chieppa.arc.plugins.manifest.CommentsAccess
import dev.chieppa.arc.plugins.manifest.NotesAccess
import dev.chieppa.arc.plugins.notes.FakeNotes
import dev.chieppa.arc.plugins.protocol.NoteKind
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.RepoInfo
import dev.chieppa.arc.plugins.protocol.SourceInfo
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.runtime.NodeLaunch
import dev.chieppa.arc.plugins.runtime.PluginTools
import dev.chieppa.arc.plugins.runtime.ToolInstaller
import dev.chieppa.arc.plugins.runtime.ToolPins
import dev.chieppa.arc.plugins.typecheck.NodeTypeChecker
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Tag
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The bundled plugin host in a real Node, named by the `arc.plugins.node` system property, as the app runs it: one
 * process for every plugin, a worker each, TypeScript converted as it is imported, the fixed permissions and a clean
 * environment. Run with `./gradlew :plugins:nodeTest -Parc.plugins.node=<node>`.
 */
@Tag("node")
class NodeRuntimeTest {
    private val dir = createTempDirectory("arc-node-runtime")
    private val sources = dir.resolve("plugins")
    private val outside = dir.resolve("outside.txt").also { Files.writeString(it, "Arc's own file") }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val launches = AtomicInteger()
    private val ids = mutableListOf<String>()

    @AfterTest
    fun cleanUp() {
        scope.cancel()
        dir.toFile().deleteRecursively()
    }

    private fun plugin(id: String, capabilities: String, files: Map<String, String>) {
        val folder = sources.resolve(id)
        Files.createDirectories(folder.resolve("src"))
        Files.writeString(folder.resolve("plugin.json"), """{"id":"$id","version":"1.0.0","apiVersion":1,"entry":"src/index.tsx","capabilities":$capabilities}""")
        files.forEach { (name, text) -> Files.writeString(folder.resolve(name), text.trimIndent()) }
        ids += id
        Files.writeString(sources.resolve("plugins.json"), ids.joinToString(",", """{"plugins":[""", "]}") { "\"$it\"" })
    }

    private fun counter() = plugin(
        "counter",
        """{"panels":true,"actions":true}""",
        mapOf(
            "src/label.ts" to """
                export function label(n: number): string {
                    return `Count ${'$'}{n}`;
                }
            """,
            "src/index.tsx" to """
                import { definePlugin, Column, Text, Button } from "@review/plugin";
                import { useState } from "react";
                import { label } from "./label";

                function Counter({ outside }: { outside: string }) {
                    const [n, setN] = useState(0);
                    let read: string;
                    try {
                        require("fs").readFileSync(outside, "utf8");
                        read = "read";
                    } catch (error: any) {
                        read = error.code;
                    }
                    return (
                        <Column>
                            <Text>{label(n)}</Text>
                            <Text>{"token " + String(process.env.GH_TOKEN) + " outside " + read}</Text>
                            <Button label="Add" onClick={() => setN(n + 1)} />
                        </Column>
                    );
                }

                export default definePlugin({
                    settings: { outside: { type: "text", default: "" } },
                    panels: [{ id: "counter", title: "Counter", render: (app) => <Counter outside={app.settings.outside} /> }],
                    actions: [{ id: "end", label: "End", run: () => process.exit(3) }],
                });
            """,
        ),
    )

    private fun broken() = plugin("broken", "{}", mapOf("src/index.tsx" to "import { definePlugin } from \"@review/plugin\";\nconst x: number = (\nexport default definePlugin({});\n"))

    private fun notes() = plugin(
        "notes",
        NOTES,
        mapOf(
            "src/index.tsx" to """
                import { definePlugin, Column, Text } from "@review/plugin";
                import type { App } from "@review/plugin";

                function Notes({ app }: { app: App }) {
                    const notes = app.useQuery(() => app.notes.list({ source: "api#1" }));
                    return <Column><Text>{notes ? notes.map((n) => `${'$'}{n.kind}:${'$'}{n.text}@${'$'}{n.path}:${'$'}{n.line}`).join(",") : "loading"}</Text></Column>;
                }

                export default definePlugin({
                    panels: [{ id: "notes", title: "Notes", render: (app) => <Notes app={app} /> }],
                    actions: [{ id: "add", label: "Add", run: async (app) => {
                        await app.notes.create({ text: "from node", path: "a.ts", line: 3 });
                        await app.comments.draft({ text: "a draft", source: "api#1" });
                    } }],
                });
            """,
        ),
    )

    private fun launch(): NodeLaunch {
        val node = Path.of(System.getProperty("arc.plugins.node"))
        val tools = PluginTools(ToolInstaller(dir.resolve("runtimes"), { _, _, _ -> throw IOException("offline") }), ToolPins(Properties()), ProcessCommandRunner, node)
        val parent = System.getenv() + mapOf("GH_TOKEN" to "gho_secret", "GITHUB_TOKEN" to "ghp_secret")
        return NodeLaunch(tools, PluginHostScript(dir.resolve("runtime"))::files, dir.resolve("work"), parent)
    }

    private fun manager(approved: Capabilities = Capabilities(panels = true, actions = true), ui: PluginUi = PluginUi.NONE): PluginManager {
        val launch = launch()
        val system = SystemProcessLauncher(scope)
        val counted = ProcessLauncher { spec -> launches.incrementAndGet(); system.launch(spec) }
        val host = NodeHost(scope, counted, launch::spec)
        val settings = buildJsonObject { put("outside", outside.toString()) }
        val config = PluginsConfig(enabled = ids.toSet(), approved = ids.associateWith { approved }, settings = mapOf("counter" to settings))
        val services = PluginServices(
            scope, host, PluginStorage.InMemory(config), ConsoleProcesses(ProcessConsoleLauncher(scope), scope), GitGrep({ _, _ -> CommandResult(1, "", "") }),
            ui = ui,
            shutdown = host::close,
        )
        return PluginManager(services).apply {
            start()
            addDirectory(sources)
        }
    }

    private fun texts(nodes: List<ViewNode>): List<String> =
        nodes.single().children.filter { it.type == "Text" }.map { text -> text.children.joinToString("") { it.text.orEmpty() } }

    @Test
    fun pluginsShareOneProcessWithoutArcsEnvironmentOrItsFiles() = runBlocking<Unit> {
        counter()
        broken()
        val manager = manager()
        try {
            val counter = manager.instance("counter")!!
            withTimeout(TIMEOUT) { counter.status.first { it is PluginStatus.Running } }
            val failed = withTimeout(TIMEOUT) { manager.instance("broken")!!.status.first { it is PluginStatus.Failed } }
            val problem = assertIs<PluginStatus.Failed>(failed).diagnostics.single()
            assertEquals(DiagnosticKind.Transpile, problem.kind)
            assertEquals("src/index.tsx" to 3, problem.file to problem.line)
            assertEquals(1, launches.get())

            val view = counter.mount(ViewKind.Panel, "counter", CallContext())
            val drawn = withTimeout(TIMEOUT) { view.nodes.first { it.isNotEmpty() } }
            assertEquals(listOf("Count 0", "token undefined outside ERR_ACCESS_DENIED"), texts(drawn))
            val button = drawn.single().children.last()
            assertEquals("Add", Props.string(button.props, "label"))
            view.event(button.id, "onClick")
            withTimeout(TIMEOUT) { view.nodes.first { texts(it).first() == "Count 1" } }
        } finally {
            manager.stop()
        }
    }

    @Test
    fun aWorkerThatEndsStartsAgainWhileTheOthersRun() = runBlocking<Unit> {
        counter()
        plugin("other", """{"panels":true}""", mapOf("src/index.tsx" to """
            import { definePlugin, Text } from "@review/plugin";
            export default definePlugin({ panels: [{ id: "hello", title: "Hello", render: () => <Text>Still here</Text> }] });
        """))
        val manager = manager()
        try {
            val counter = manager.instance("counter")!!
            val other = manager.instance("other")!!
            withTimeout(TIMEOUT) { counter.status.first { it is PluginStatus.Running } }
            withTimeout(TIMEOUT) { other.status.first { it is PluginStatus.Running } }
            counter.runAction("end", CallContext())
            withTimeout(TIMEOUT) { counter.status.first { it is PluginStatus.Restarting } }
            val view = other.mount(ViewKind.Panel, "hello", CallContext())
            val drawn = withTimeout(TIMEOUT) { view.nodes.first { it.isNotEmpty() } }
            assertEquals("Still here", drawn.single().children.joinToString("") { it.text.orEmpty() })
            withTimeout(TIMEOUT) { counter.status.first { it is PluginStatus.Running } }
            assertTrue(other.status.value is PluginStatus.Running)
            assertEquals(1, launches.get())
        } finally {
            manager.stop()
        }
    }

    @Test
    fun aPluginReadsAndAddsNotesAndDraftsThroughTheAppObject() = runBlocking<Unit> {
        notes()
        val window = FakeNotes()
        val manager = manager(NOTES_APPROVED, window)
        try {
            val plugin = manager.instance("notes")!!
            withTimeout(TIMEOUT) { plugin.status.first { it is PluginStatus.Running } }
            plugin.runAction("add", CallContext(SourceInfo("api#1", "pull", RepoInfo("api", "api"))))
            val added = withTimeout(TIMEOUT) { window.all.first { it.size == 2 } }
            assertEquals(listOf(NoteKind.Note, NoteKind.Draft), added.map { it.kind })
            assertEquals("api#1", added.first().source)
            val view = plugin.mount(ViewKind.Panel, "notes", CallContext())
            val shown = withTimeout(TIMEOUT) { view.nodes.first { it.isNotEmpty() && texts(it) != listOf("loading") } }
            assertEquals(listOf("note:from node@a.ts:3"), texts(shown))
        } finally {
            manager.stop()
        }
    }

    @Test
    fun aPluginsTypeErrorsAreFoundInAProcessOfItsOwnAgainstTheSdksTypes() = runBlocking<Unit> {
        plugin("typed", "{}", mapOf("src/index.tsx" to """
            import { definePlugin } from "@review/plugin";
            import type { GrepHit } from "@review/plugin";
            import { useState } from "react";

            export function first(hits: GrepHit[]): number {
                const [n] = useState(0);
                return hits[n].lines;
            }

            export default definePlugin({});
        """))
        val errors = NodeTypeChecker(launch()::checkSpec).check(sources.resolve("typed"), "src/index.tsx")
        val error = errors.single()
        assertEquals(DiagnosticKind.Type, error.kind)
        assertEquals("src/index.tsx" to 7, error.file to error.line)
        assertTrue("'lines' does not exist on type 'GrepHit'" in error.message, error.message)
    }

    private companion object {
        const val TIMEOUT = 30_000L
        const val NOTES = """{"panels":true,"actions":true,"notes":{"read":true,"write":true},"comments":{"draft":true}}"""
        val NOTES_APPROVED = Capabilities(panels = true, actions = true, notes = NotesAccess(read = true, write = true), comments = CommentsAccess(draft = true))
    }
}
