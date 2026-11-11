package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.bundle.CommandRunner
import java.io.IOException
import java.nio.file.Path

/**
 * Node and esbuild for plugins: the programs the system properties [NODE_PROPERTY] and [ESBUILD_PROPERTY] name, for
 * developing with ones already installed, else the pinned ones, installed by [installer] the first time they are
 * needed. A named Node's version is asked of it through [runner].
 */
class PluginTools(
    private val installer: ToolInstaller,
    private val pins: ToolPins,
    private val runner: CommandRunner,
    private val nodeOverride: Path? = System.getProperty(NODE_PROPERTY)?.let(Path::of),
    private val esbuildOverride: Path? = System.getProperty(ESBUILD_PROPERTY)?.let(Path::of),
) {
    @Volatile
    private var node: NodeProgram? = null

    /** Node, installed if needed. Throws [IOException] when there is none for this host or it cannot be had. */
    suspend fun node(): NodeProgram {
        node?.let { return it }
        val found = nodeOverride?.let { NodeProgram(it, versionOf(it)) } ?: run {
            val pin = pins.pin(ToolPins.NODE) ?: throw IOException("$NO_PIN ${ToolPins.NODE} ${HostPlatform.current}")
            NodeProgram(installer.install(pin), major(pin.version))
        }
        return found.also { node = it }
    }

    /** esbuild, installed if needed. Throws [IOException] when there is none for this host or it cannot be had. */
    suspend fun esbuild(): Path = esbuildOverride ?: run {
        val pin = pins.pin(ToolPins.ESBUILD) ?: throw IOException("$NO_PIN ${ToolPins.ESBUILD} ${HostPlatform.current}")
        installer.install(pin)
    }

    /** Whether the pinned Node still has to be downloaded. */
    fun needsDownload(): Boolean = nodeOverride == null && pins.pin(ToolPins.NODE)?.let { !installer.installed(it) } ?: false

    /** The pinned Node's version. */
    val pinnedNode: String? get() = pins.version(ToolPins.NODE)

    private suspend fun versionOf(program: Path): Int {
        val result = runner.run(listOf(program.toString(), VERSION_FLAG), program.parent ?: Path.of("."))
        return major(result.output.trim())
    }

    companion object {
        const val NODE_PROPERTY = "arc.plugins.node"
        const val ESBUILD_PROPERTY = "arc.plugins.esbuild"
        private const val VERSION_FLAG = "--version"
        private const val NO_PIN = "Nothing pinned for"

        /** The major version in `v26.13.0` or `26.13.0`; 0 when there is none. */
        fun major(version: String): Int = version.removePrefix("v").substringBefore('.').toIntOrNull() ?: 0
    }
}
