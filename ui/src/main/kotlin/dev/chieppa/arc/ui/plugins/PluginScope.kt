package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.console.ConsoleProcess
import dev.chieppa.arc.plugins.manager.PluginView
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * What a plugin view's components draw with: the [view] whose handlers they call, the processes they show, and whether
 * secret settings outlast this run of Arc ([secretsLast]), which a secret field shows when they do not.
 */
class PluginScope(val view: PluginView, val secretsLast: Boolean = true, private val consoles: (String) -> ConsoleProcess?) {
    /** Calls node [node]'s [handler] prop in the plugin with [args]. */
    fun call(node: Int, handler: String, vararg args: JsonElement) = view.event(node, handler, JsonArray(args.toList()))

    /** The process [handle] names, if it still runs or ran. */
    fun console(handle: String): ConsoleProcess? = consoles(handle)?.takeIf { it.plugin == view.id.substringBefore('/') }
}
