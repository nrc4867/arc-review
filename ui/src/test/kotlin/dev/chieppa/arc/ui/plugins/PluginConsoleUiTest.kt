package dev.chieppa.arc.ui.plugins

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.console.ConsoleProcess
import dev.chieppa.arc.plugins.manager.PluginView
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.components.PluginConsole
import dev.chieppa.stretch.test.setStretchContent
import dev.chieppa.stretch.theme.StretchThemes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

/** A console shows its process's prompt before its line ends, and only that process's output once its handle changes. */
@OptIn(ExperimentalTestApi::class)
class PluginConsoleUiTest {
    @Test
    fun aNewHandleStartsAFreshConsole() = runComposeUiTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(mainClock.scheduler))
        try {
            val first = FakeConsole().also { it.print("Password: ") }
            val second = FakeConsole().also { it.print("Password: ") }
            val processes = mapOf("todos-1" to ConsoleProcess("todos-1", "todos", listOf("a"), first, scope), "todos-2" to ConsoleProcess("todos-2", "todos", listOf("b"), second, scope))
            val view = PluginView("todos/1", ViewKind.Settings, "", CallContext(), JsonObject(emptyMap()), { _, _, _, _ -> }, {})
            val pluginScope = PluginScope(view) { processes[it] }
            var handle by mutableStateOf("todos-1")
            setStretchContent(StretchThemes.WorkshopDark) {
                PluginConsole(ViewNode(1, "Console", JsonObject(mapOf(PropNames.HANDLE to JsonPrimitive(handle)))), pluginScope)
            }
            waitForIdle()
            handle = "todos-2"
            waitForIdle()
            assertEquals(1, onAllNodes(hasText("Password:", substring = true)).fetchSemanticsNodes().size)
            assertEquals(0, onAllNodes(hasText("Password: Password:", substring = true)).fetchSemanticsNodes().size)
        } finally {
            scope.cancel()
        }
    }
}
