package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.host.Preparation
import dev.chieppa.arc.plugins.manifest.PluginManifest
import kotlinx.serialization.json.JsonObject
import java.nio.file.Path

/**
 * Makes a plugin in [folder] with [manifest] ready to start, with its stored [settings]; [root] is the folder its source
 * reads from, which the Node process is let read.
 */
fun interface PluginPreparer {
    suspend fun prepare(folder: Path, root: Path, manifest: PluginManifest, settings: JsonObject): Preparation
}
