package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.manifest.Capability
import dev.chieppa.arc.plugins.source.FoundPlugin

/**
 * A plugin Arc knows of: where it was [found], whether the person [enabled] it, and its [instance] while it runs. An
 * enabled plugin whose manifest asks for capabilities the person has not approved does not run; [asks] lists them.
 */
data class PluginEntry(val found: FoundPlugin, val enabled: Boolean, val instance: PluginInstance?, val asks: List<Capability> = emptyList()) {
    val id: String get() = found.id

    /** Whether it is enabled with everything it asks for approved. */
    val on: Boolean get() = enabled && asks.isEmpty()
}
