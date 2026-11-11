package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/**
 * What a plugin declares it may do. The contribution kinds ([panels], [panes], [actions]) let it add to Arc's window;
 * [console] lets it run processes through `app.console.run`, [git] lets it query the open sources' repos. [files]
 * names folders its own process may read and write, relative to its folder or absolute; [network] the hosts it talks
 * to, any of which lets its process use the network; [processes] lets its own process start programs. [mcp] names the
 * MCP server roles it calls. Arc starts the plugin's Node process with only these granted and checks every call into
 * the app against them.
 */
@Serializable
data class Capabilities(
    val panels: Boolean = false,
    val panes: Boolean = false,
    val actions: Boolean = false,
    val console: Boolean = false,
    val git: Boolean = false,
    val files: FileAccess = FileAccess(),
    val network: List<String> = emptyList(),
    val processes: Boolean = false,
    val mcp: List<String> = emptyList(),
)
