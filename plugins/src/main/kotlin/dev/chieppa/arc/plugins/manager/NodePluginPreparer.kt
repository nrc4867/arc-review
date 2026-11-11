package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.bundle.BundleResult
import dev.chieppa.arc.plugins.bundle.EsbuildBundler
import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.manifest.ApiVersion
import dev.chieppa.arc.plugins.manifest.PluginManifest
import dev.chieppa.arc.plugins.protocol.InitializeParams
import dev.chieppa.arc.plugins.protocol.PluginInfo
import dev.chieppa.arc.plugins.runtime.PermissionFlags
import dev.chieppa.arc.plugins.runtime.PluginTools
import kotlinx.serialization.json.JsonObject
import java.io.IOException
import java.nio.file.Path

/**
 * Prepares a plugin to run in Node: bundles its entry with esbuild into [cacheDir]`/<id>/bundle.js`, and starts the
 * host script [hostScript] gives in [PluginTools.node] under the permission flags its capabilities allow, with source
 * maps on so errors name the plugin's own files.
 */
class NodePluginPreparer(
    private val tools: PluginTools,
    private val bundler: EsbuildBundler,
    private val hostScript: suspend () -> Path,
    private val cacheDir: Path,
) : PluginPreparer {
    override suspend fun prepare(folder: Path, manifest: PluginManifest, settings: JsonObject): Preparation {
        val out = cacheDir.resolve(manifest.id).resolve(BUNDLE)
        val bundle = when (val result = bundler.bundle(folder, manifest.entry, out)) {
            is BundleResult.Failed -> return Preparation.Failed(result.diagnostics)
            is BundleResult.Bundled -> result.file
        }
        val (node, script) = try {
            tools.node() to hostScript()
        } catch (e: IOException) {
            return Preparation.Failed(listOf(PluginDiagnostic(DiagnosticKind.Load, e.message ?: e.toString())))
        }
        val flags = PermissionFlags.of(manifest.capabilities, folder.toAbsolutePath(), listOf(script, bundle.parent).map { it.toAbsolutePath() }, node.major)
        val command = listOf(node.path.toString()) + flags + SOURCE_MAPS + script.toString()
        val initialize = InitializeParams(
            ApiVersion.CURRENT,
            PluginInfo(manifest.id, manifest.version, folder.toAbsolutePath().toString()),
            bundle.toAbsolutePath().toString(),
            settings,
            manifest.capabilities,
        )
        return Preparation.Ready(LaunchSpec(command, folder), initialize)
    }

    private companion object {
        const val BUNDLE = "bundle.js"
        const val SOURCE_MAPS = "--enable-source-maps"
    }
}
