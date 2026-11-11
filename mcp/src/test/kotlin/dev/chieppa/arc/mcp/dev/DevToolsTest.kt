package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ArcTool
import dev.chieppa.arc.mcp.tools.ToolAnswer
import dev.chieppa.arc.mcp.tools.ToolPart
import dev.chieppa.arc.mcp.tools.ToolRefusal
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.protocol.AppEvents
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.RepoInfo
import dev.chieppa.arc.plugins.protocol.SourceInfo
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.view.ViewNode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DevToolsTest {
    private val running = DevPlugin("hello", "Hello", "/p/hello", directory = true, enabled = true, state = DevPluginState.Running, actions = listOf("say"), panels = listOf("panel"), panes = listOf("pane"))
    private val host = FakeDevHost(
        listOf(
            running,
            DevPlugin("git-one", "Git one", "/cache/git-one", directory = false, enabled = true, state = DevPluginState.Running, actions = listOf("say")),
            DevPlugin("off", "Off", "/p/off", directory = true, enabled = false, state = DevPluginState.Off),
            DevPlugin("asks", "Asks", "/p/asks", directory = true, enabled = true, state = DevPluginState.WaitingForApproval, asks = listOf("network api.example.com")),
            DevPlugin("broken", "Broken", "/p/broken", directory = true, enabled = true, state = DevPluginState.Failed),
        ),
    )
    private val captured = mutableListOf<String>()
    private val window = object : DevWindow {
        val openedViews = mutableListOf<String>()

        override suspend fun context(ref: String?): CallContext? = when (ref) {
            null -> CallContext()
            "api#1" -> CallContext(SourceInfo("api#1", "pull", RepoInfo("github.com/a/api", "api")))
            else -> null
        }

        override suspend fun open(plugin: String, kind: ViewKind, contribution: String) {
            openedViews += "$plugin/$kind/$contribution"
        }

        override suspend fun capture(view: String): ByteArray = byteArrayOf(9).also { captured += view }
    }
    private val tools = DevTools.all(host, window, DevTimings(actionMillis = 1_000)).associateBy { it.name }

    private fun tool(name: String): ArcTool = tools.getValue(name)

    private suspend fun call(name: String, vararg args: Pair<String, Any>): ToolAnswer = tool(name).call(
        JsonObject(args.associate { (key, value) -> key to (value as? JsonObject ?: if (value is Boolean) JsonPrimitive(value) else if (value is Int) JsonPrimitive(value) else JsonPrimitive(value.toString())) }),
    )

    private fun text(answer: ToolAnswer): String = answer.parts.filterIsInstance<ToolPart.Text>().joinToString("\n") { it.text }

    @Test
    fun everyToolHasItsNameDescriptionAndSchema() {
        assertEquals(DevToolNames.ALL.toSet(), tools.keys)
        tools.values.forEach { assertTrue(it.description.length > 40, it.name) }
        assertEquals(listOf(DevArgs.PLUGIN), tool(DevToolNames.TYPE_CHECK).input.required)
        assertEquals(listOf(DevArgs.PLUGIN, DevArgs.ACTION), tool(DevToolNames.RUN_ACTION).input.required)
    }

    @Test
    fun theStarterGuideNamesEveryTool() {
        val guide = DevToolsTest::class.java.getResourceAsStream("/dev/chieppa/arc/plugins/starter/CLAUDE.md.template")!!.use { it.readAllBytes().toString(Charsets.UTF_8) }
        DevToolNames.ALL.forEach { assertTrue("`$it`" in guide, it) }
    }

    @Test
    fun theListShowsDirectoryPluginsWhateverTheirStateButNoGitOnes() = runTest {
        val listed = Json.parseToJsonElement(text(call(DevToolNames.LIST))).jsonArray.map { it.jsonObject.getValue("id").jsonPrimitive.content }
        assertEquals(listOf("hello", "off", "asks", "broken"), listed)
        assertTrue("\"waiting for approval\"" in text(call(DevToolNames.LIST)))
    }

    @Test
    fun noToolReachesAGitPluginAnOffOneOrOneWaitingForApproval() = runTest {
        val perPlugin = DevToolNames.ALL - DevToolNames.LIST
        for (name in perPlugin) {
            for (id in listOf("git-one", "off", "asks", "nobody")) {
                val refusal = assertFailsWith<ToolRefusal>("$name $id") { call(name, DevArgs.PLUGIN to id, DevArgs.ACTION to "say", DevArgs.VIEW to "panel", DevArgs.EVENT to AppEvents.SOURCE_OPENED) }
                assertTrue(id in refusal.message.orEmpty(), refusal.message)
            }
        }
        assertEquals(emptyList(), host.reached)
        assertTrue("approve" in assertFailsWith<ToolRefusal> { call(DevToolNames.RELOAD, DevArgs.PLUGIN to "asks") }.message.orEmpty())
    }

    @Test
    fun aFailedPluginCanBeCheckedReloadedAndReadButNotDriven() = runTest {
        host.logs["broken"] = listOf("TypeError: x is undefined")
        assertTrue("x is undefined" in text(call(DevToolNames.LOG, DevArgs.PLUGIN to "broken")))
        call(DevToolNames.TYPE_CHECK, DevArgs.PLUGIN to "broken")
        call(DevToolNames.RELOAD, DevArgs.PLUGIN to "broken")
        val refusal = assertFailsWith<ToolRefusal> { call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "broken", DevArgs.ACTION to "say") }
        assertTrue("failed" in refusal.message.orEmpty() && DevToolNames.LOG in refusal.message.orEmpty(), refusal.message)
    }

    @Test
    fun aTypeCheckGivesEveryErrorWithItsColumnFromOne() = runTest {
        host.errors["hello"] = (1..7).map { PluginDiagnostic(DiagnosticKind.Type, "error $it", "src/index.tsx", it, 4) }
        val errors = Json.parseToJsonElement(text(call(DevToolNames.TYPE_CHECK, DevArgs.PLUGIN to "hello"))).jsonArray
        assertEquals(7, errors.size)
        assertEquals("5", errors.first().jsonObject.getValue("column").jsonPrimitive.content)
        host.errors["hello"] = emptyList()
        assertEquals(DevMessages.NO_ERRORS, text(call(DevToolNames.TYPE_CHECK, DevArgs.PLUGIN to "hello")))
    }

    @Test
    fun theLogGivesItsLastLines() = runTest {
        host.logs["hello"] = (1..300).map { "line $it" }
        val log = Json.parseToJsonElement(text(call(DevToolNames.LOG, DevArgs.PLUGIN to "hello", DevArgs.LINES to 2))).jsonObject
        assertEquals(listOf("line 299", "line 300"), log.getValue("lines").jsonArray.map { it.jsonPrimitive.content })
        assertEquals("300", log.getValue("total").jsonPrimitive.content)
    }

    @Test
    fun aViewsTreeIsIndentedTextWithItsHandlersMarked() = runTest {
        val button = ViewNode(3, "Button", buildJsonObject { put("label", "Press"); put("onClick", buildJsonObject { put("\$handler", true) }) })
        val tree = listOf(ViewNode(1, "Column", buildJsonObject { put("gap", 8) }, listOf(ViewNode(2, "Text", children = listOf(ViewNode(4, ViewNode.TEXT, text = "api#1"))), button)))
        host.views["hello"] = listOf(DevView("hello/1", ViewKind.Panel, "panel", tree))
        val text = text(call(DevToolNames.VIEW_TREE, DevArgs.PLUGIN to "hello", DevArgs.VIEW to "panel"))
        assertEquals("panel panel (hello/1)\nColumn #1 gap=8\n  Text #2\n    \"api#1\"\n  Button #3 label=\"Press\" onClick=<handler>", text)
    }

    @Test
    fun aViewNotOnScreenIsOpenedOnlyWhenAsked() = runTest {
        assertFailsWith<ToolRefusal> { call(DevToolNames.SCREENSHOT, DevArgs.PLUGIN to "hello", DevArgs.VIEW to "pane") }
        assertEquals(emptyList(), window.openedViews)
        host.opened["pane"] = DevView("hello/2", ViewKind.Pane, "pane", listOf(ViewNode(1, "Text")))
        val picture = call(DevToolNames.SCREENSHOT, DevArgs.PLUGIN to "hello", DevArgs.VIEW to "pane", DevArgs.OPEN to true)
        assertEquals(listOf("hello/Pane/pane"), window.openedViews)
        assertEquals(listOf("hello/2"), captured)
        assertEquals(9, assertIs<ToolPart.Png>(picture.parts.first()).bytes.single().toInt())
        assertFailsWith<ToolRefusal> { call(DevToolNames.VIEW_TREE, DevArgs.PLUGIN to "hello", DevArgs.VIEW to "settings") }
    }

    @Test
    fun anActionRunsOnTheSourceNamedAndOneWaitingOnThePersonIsReportedNotAwaited() = runTest {
        val ran = mutableListOf<String>()
        host.action = { ran += it }
        assertEquals(DevMessages.ACTION_DONE, text(call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "hello", DevArgs.ACTION to "say", DevArgs.SOURCE to "api#1")))
        assertEquals(listOf("say"), ran)
        assertFailsWith<ToolRefusal> { call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "hello", DevArgs.ACTION to "say", DevArgs.SOURCE to "web#9") }
        assertFailsWith<ToolRefusal> { call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "hello", DevArgs.ACTION to "push") }
        host.action = host.waiting
        assertEquals(DevMessages.ACTION_WAITING, text(call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "hello", DevArgs.ACTION to "say")))
        host.action = { throw IllegalStateException("boom") }
        val failed = call(DevToolNames.RUN_ACTION, DevArgs.PLUGIN to "hello", DevArgs.ACTION to "say")
        assertTrue(failed.error && "boom" in text(failed))
    }

    @Test
    fun anEventGoesToThatPluginAloneWithItsSourceAndData() = runTest {
        val data = buildJsonObject { put("kind", "Reviewed") }
        call(DevToolNames.FIRE_EVENT, DevArgs.PLUGIN to "hello", DevArgs.EVENT to AppEvents.FILE_MARKED, DevArgs.SOURCE to "api#1", DevArgs.DATA to data)
        val (id, event) = host.events.single()
        assertEquals("hello" to "api#1", id to event.context.source?.ref)
        assertEquals(data, event.data)
        assertFailsWith<ToolRefusal> { call(DevToolNames.FIRE_EVENT, DevArgs.PLUGIN to "hello", DevArgs.EVENT to "push.started") }
        host.deaf += AppEvents.NOTES_CHANGED
        assertFailsWith<ToolRefusal> { call(DevToolNames.FIRE_EVENT, DevArgs.PLUGIN to "hello", DevArgs.EVENT to AppEvents.NOTES_CHANGED) }
    }
}
