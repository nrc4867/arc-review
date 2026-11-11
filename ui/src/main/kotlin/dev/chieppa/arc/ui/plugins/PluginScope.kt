package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.console.ConsoleProcess
import dev.chieppa.arc.plugins.manager.PluginView
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/** What a plugin view's components draw with: the [view] whose handlers they call, and the processes they show. */
class PluginScope(val view: PluginView, private val consoles: (String) -> ConsoleProcess?) {
    /** Calls node [node]'s [handler] prop in the plugin with [args]. */
    fun call(node: Int, handler: String, vararg args: JsonElement) = view.event(node, handler, JsonArray(args.toList()))

    /** The process [handle] names, if it still runs or ran. */
    fun console(handle: String): ConsoleProcess? = consoles(handle)?.takeIf { it.plugin == view.id.substringBefore('/') }
}
