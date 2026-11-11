package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.host.WorkerSpec
import dev.chieppa.arc.plugins.manifest.ApiVersion
import dev.chieppa.arc.plugins.manifest.PluginManifest
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import kotlinx.serialization.json.JsonObject
import java.nio.file.Files
import java.nio.file.Path

/**
 * Prepares a plugin to run from its own files: its worker loads the manifest's entry, converting TypeScript as it is
 * imported, so nothing is built or written first. A missing entry fails at once, naming the file.
 */
object EntryPreparer : PluginPreparer {
    private const val NO_ENTRY = "No entry file"

    override suspend fun prepare(folder: Path, root: Path, manifest: PluginManifest, settings: JsonObject): Preparation {
        val entry = folder.resolve(manifest.entry).toAbsolutePath().normalize()
        if (!Files.isRegularFile(entry)) return Preparation.Failed(listOf(PluginDiagnostic(DiagnosticKind.Load, NO_ENTRY, manifest.entry)))
        val initialize = InitializeParams(
            ApiVersion.CURRENT,
            PluginInfo(manifest.id, manifest.version, folder.toAbsolutePath().toString()),
            entry.toString(),
            settings,
            manifest.capabilities,
        )
        return Preparation.Ready(WorkerSpec(manifest.id, root), initialize)
    }
}
