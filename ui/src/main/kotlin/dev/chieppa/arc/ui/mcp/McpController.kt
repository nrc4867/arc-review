package dev.chieppa.arc.ui.mcp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.mcp.dev.DevTools
import dev.chieppa.arc.mcp.dev.ManagerPluginHost
import dev.chieppa.arc.mcp.server.ConnectCommand
import dev.chieppa.arc.mcp.server.McpServerControl
import dev.chieppa.arc.mcp.server.McpServerState
import dev.chieppa.arc.mcp.server.McpSetup
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The window's side of Arc's MCP server, as [setup] sets it up (none where the app serves no MCP): switching it on and
 * off, its state as Compose state, the command that connects Claude Code, and the plugin views it pictures. Its tools are
 * the plugin development tools over the window's plugins.
 */
class McpController(private val workbench: Workbench, private val setup: McpSetup?) {
    /** The plugin views on screen, which the screenshot tool pictures. */
    val captures = ViewCaptures()

    private val control: McpServerControl? = setup?.let { s ->
        McpServerControl(CoroutineScope(workbench.scope.coroutineContext + workbench.dispatchers.io), s.token, s.port, { token -> s.endpoint(tools(), token) }, s.onChange)
    }

    /** Whether the app serves MCP at all. */
    val available: Boolean get() = control != null

    /** Where the server stands. */
    var state by mutableStateOf<McpServerState>(McpServerState.Off)
        private set

    /** How many clients are connected now. */
    val clients: Int get() = (state as? McpServerState.Listening)?.clients ?: 0

    /** Whether the token outlasts this run of Arc, kept in the system's keychain. */
    val tokenLasts: Boolean get() = setup?.token?.lasting ?: true

    /** The command that adds the server to Claude Code, with its token. */
    fun command(): String? = control?.let { ConnectCommand.of(it.port, it.token.current()) }

    /** Follows the server's state, starting it when the person left it switched on. */
    fun start() {
        val control = control ?: return
        workbench.scope.launch { control.state.collect { state = it } }
        if (setup?.startOn == true) control.start()
    }

    /** Switches it [on] or off, as the person does in Settings. */
    fun switch(on: Boolean) {
        control?.switch(on)
    }

    fun stop() {
        control?.stop()
    }

    private fun tools() = workbench.plugins.manager?.let { DevTools.all(ManagerPluginHost(it), WindowDevWindow(workbench, captures)) }.orEmpty()
}
