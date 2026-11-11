package dev.chieppa.arc.plugins.source

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.manifest.PluginManifest
import java.nio.file.Path

/** A plugin folder found in [source]: its [manifest], or null with the [diagnostic] saying why it cannot be read. */
data class FoundPlugin(
    val source: PluginSourceSpec,
    val folder: Path,
    val manifest: PluginManifest?,
    val diagnostic: PluginDiagnostic? = null,
) {
    /** The plugin's id, or its folder's name while its manifest cannot be read. */
    val id: String get() = manifest?.id ?: folder.fileName.toString()
}
