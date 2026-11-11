package dev.chieppa.arc.plugins.manifest

import kotlinx.serialization.Serializable

/**
 * What a plugin declares it may do through the app object, which Arc checks every call against. The contribution kinds
 * ([panels], [panes], [actions]) let it add to Arc's window; [console] lets it run programs through `app.console.run`,
 * [git] lets it query the open sources' repos, [files] names the folders `app.files` may read and write (relative to its
 * folder or absolute), [network] the hosts `app.fetch` may reach, and [mcp] the MCP server roles it calls. Its own code
 * runs with none of these: in a worker of a Node process that reads only the plugins' folders.
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
    val mcp: List<String> = emptyList(),
)
