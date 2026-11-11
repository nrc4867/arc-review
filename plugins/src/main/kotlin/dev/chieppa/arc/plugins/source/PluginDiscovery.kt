package dev.chieppa.arc.plugins.source

import dev.chieppa.arc.plugins.config.PluginSourceSpec
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.manifest.ManifestFiles
import dev.chieppa.arc.plugins.manifest.ManifestReader
import dev.chieppa.arc.plugins.manifest.ManifestResult
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/** Finds the plugins a source holds: one folder with a `plugin.json`, or a folder whose `plugins.json` lists several. */
object PluginDiscovery {
    fun find(source: PluginSourceSpec): List<FoundPlugin> = when (source) {
        is PluginSourceSpec.Directory -> directory(source, Path.of(source.path))
        is PluginSourceSpec.Git -> emptyList()
    }

    /** The folder [plugin]'s source reads from: the source's folder, or the plugin's own when it lies outside it. */
    fun root(plugin: FoundPlugin): Path {
        val folder = plugin.folder.toAbsolutePath().normalize()
        val source = (plugin.source as? PluginSourceSpec.Directory)?.let { Path.of(it.path).toAbsolutePath().normalize() } ?: return folder
        return if (folder.startsWith(source)) source else folder
    }

    private fun directory(source: PluginSourceSpec, folder: Path): List<FoundPlugin> {
        val list = folder.resolve(ManifestFiles.PLUGINS)
        if (!Files.isRegularFile(list)) return listOf(plugin(source, folder))
        return when (val read = ManifestReader.list(readText(list) ?: "")) {
            is ManifestResult.Read -> read.value.plugins.map { plugin(source, folder.resolve(it).normalize()) }
            is ManifestResult.Invalid -> listOf(FoundPlugin(source, folder, null, PluginDiagnostic(DiagnosticKind.Manifest, read.reason, ManifestFiles.PLUGINS)))
        }
    }

    private fun plugin(source: PluginSourceSpec, folder: Path): FoundPlugin {
        val text = readText(folder.resolve(ManifestFiles.PLUGIN))
            ?: return FoundPlugin(source, folder, null, PluginDiagnostic(DiagnosticKind.Manifest, NO_MANIFEST, ManifestFiles.PLUGIN))
        return when (val read = ManifestReader.plugin(text)) {
            is ManifestResult.Read -> FoundPlugin(source, folder, read.value)
            is ManifestResult.Invalid -> FoundPlugin(source, folder, null, PluginDiagnostic(DiagnosticKind.Manifest, read.reason, ManifestFiles.PLUGIN))
        }
    }

    private fun readText(file: Path): String? = try {
        if (Files.isRegularFile(file)) Files.readString(file) else null
    } catch (_: IOException) {
        null
    }

    private const val NO_MANIFEST = "No plugin.json"
}
