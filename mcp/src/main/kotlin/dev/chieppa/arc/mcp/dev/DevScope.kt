package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.mcp.tools.ToolRefusal

/**
 * Which plugins the development tools reach: only those from a folder source that the person has enabled, with all
 * they ask for approved. Git-sourced plugins, plugins that are off or waiting for approval, and Arc's own features are
 * out of reach.
 */
object DevScope {
    /** The directory plugins, which the list shows whatever their state. */
    fun listed(host: DevPluginHost): List<DevPlugin> = host.plugins().filter { it.directory }

    /** Plugin [id] when the tools may reach it; else throws [ToolRefusal] saying why. */
    fun reach(host: DevPluginHost, id: String): DevPlugin {
        val plugin = host.plugins().firstOrNull { it.id == id } ?: throw ToolRefusal("${DevMessages.UNKNOWN} $id")
        return when {
            !plugin.directory -> throw ToolRefusal("$id ${DevMessages.GIT}")
            !plugin.enabled -> throw ToolRefusal("$id ${DevMessages.OFF}")
            plugin.state == DevPluginState.WaitingForApproval -> throw ToolRefusal("$id ${DevMessages.WAITING} ${plugin.asks.joinToString(", ")}")
            plugin.state == DevPluginState.Unreadable -> throw ToolRefusal("$id ${DevMessages.UNREADABLE} ${plugin.problems.joinToString("; ") { it.message }}")
            else -> plugin
        }
    }

    /** Plugin [id] when the tools may reach it and it runs; else throws [ToolRefusal] saying why. */
    fun running(host: DevPluginHost, id: String): DevPlugin {
        val plugin = reach(host, id)
        if (plugin.state != DevPluginState.Running) throw ToolRefusal("$id ${DevMessages.NOT_RUNNING} ${DevJson.name(plugin.state)}. ${DevMessages.READ_LOG}")
        return plugin
    }
}
