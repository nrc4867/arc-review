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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The window's side of Arc's MCP server, as [setup] sets it up (none where the app serves no MCP): switching it on and
 * off, its state as Compose state, the command that connects Claude Code and whether its token is kept, and the plugin
 * views it pictures. Its tools are the plugin development tools over the window's plugins. Nothing here touches the
 * keychain on the window's thread, and what fails becomes state, never a throw.
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

    /** The command that adds the server to Claude Code, with its token, once the server is on and the token read. */
    var command by mutableStateOf<String?>(null)
        private set

    /** Whether the token outlasts this run of Arc, kept in the system's keychain. */
    var tokenLasts by mutableStateOf(true)
        private set

    /** Why the token is not kept, or could not be had at all, or null when nothing is wrong with it. */
    var tokenProblem by mutableStateOf<String?>(null)
        private set

    /** Follows the server's state, starting it when the person left it switched on, and reads the token once it is on. */
    fun start() {
        val control = control ?: return
        workbench.scope.launch {
            control.state.collect {
                state = it
                if (it != McpServerState.Off && command == null) readToken(control)
            }
        }
        if (setup?.startOn == true) control.start()
    }

    /** Switches it [on] or off, as the person does in Settings. */
    fun switch(on: Boolean) {
        control?.switch(on)
    }

    fun stop() {
        control?.stop()
    }

    /** Reads the token away from the window's thread, since the keychain may be slow or ask the person first. */
    private suspend fun readToken(control: McpServerControl) {
        val token = control.token
        val command = try {
            withContext(workbench.dispatchers.io) { ConnectCommand.of(control.port, token.current()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            tokenProblem = e.message ?: e.toString()
            tokenLasts = false
            return
        }
        this.command = command
        tokenLasts = token.lasting
        tokenProblem = token.problem
    }

    private fun tools() = workbench.plugins.manager?.let { DevTools.all(ManagerPluginHost(it), WindowDevWindow(workbench, captures)) }.orEmpty()
}
