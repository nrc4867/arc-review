package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.source.FoundPlugin

/** A plugin Arc knows of: where it was [found], whether the person [enabled] it, and its [instance] while enabled. */
data class PluginEntry(val found: FoundPlugin, val enabled: Boolean, val instance: PluginInstance?) {
    val id: String get() = found.id
}
