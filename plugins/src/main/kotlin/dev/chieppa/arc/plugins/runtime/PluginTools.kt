package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.command.CommandRunner
import dev.chieppa.arc.tools.HostPlatform
import dev.chieppa.arc.tools.ToolInstaller
import dev.chieppa.arc.tools.ToolPins
import java.io.IOException
import java.nio.file.Path

/**
 * The Node plugins run on: the program the system property [NODE_PROPERTY] names, for developing with one already
 * installed, else the pinned one, installed by [installer] the first time it is needed. A named Node's version is asked
 * of it through [runner]; one older than [MIN_MAJOR] is refused, as it cannot keep plugins off the network.
 */
class PluginTools(
    private val installer: ToolInstaller,
    private val pins: ToolPins,
    private val runner: CommandRunner,
    private val nodeOverride: Path? = System.getProperty(NODE_PROPERTY)?.let(Path::of),
) {
    @Volatile
    private var node: NodeProgram? = null

    /** Node, installed if needed. Throws [IOException] when there is none for this host, it cannot be had or is too old. */
    suspend fun node(): NodeProgram {
        node?.let { return it }
        val found = nodeOverride?.let { NodeProgram(it, versionOf(it)) } ?: run {
            val pin = pins.pin(NODE) ?: throw IOException("$NO_PIN ${NODE} ${HostPlatform.current}")
            NodeProgram(installer.install(pin), major(pin.version))
        }
        if (found.major < MIN_MAJOR) throw IOException("$TOO_OLD $MIN_MAJOR: ${found.path}")
        return found.also { node = it }
    }

    /** Whether the pinned Node still has to be downloaded. */
    fun needsDownload(): Boolean = nodeOverride == null && pins.pin(NODE)?.let { !installer.installed(it) } ?: false

    /** The pinned Node's version. */
    val pinnedNode: String? get() = pins.version(NODE)

    private suspend fun versionOf(program: Path): Int {
        val result = runner.run(listOf(program.toString(), VERSION_FLAG), program.parent ?: Path.of("."))
        return major(result.output.trim())
    }

    companion object {
        const val NODE_PROPERTY = "arc.plugins.node"

        /** Node's name in `tools.properties`. */
        const val NODE = "node"
        private const val PINS = "/dev/chieppa/arc/plugins/runtime/tools.properties"

        /** The first Node whose permission model covers the network. */
        const val MIN_MAJOR = 25
        private const val VERSION_FLAG = "--version"
        private const val NO_PIN = "Nothing pinned for"
        private const val TOO_OLD = "Plugins need Node"

        /** The pins Arc ships with for plugins. */
        fun bundledPins(): ToolPins = ToolPins.resource(PluginTools::class.java, PINS)

        /** The major version in `v26.13.0` or `26.13.0`; 0 when there is none. */
        fun major(version: String): Int = version.removePrefix("v").substringBefore('.').toIntOrNull() ?: 0
    }
}
