package dev.chieppa.arc.plugins.runtime

import java.util.Properties

/**
 * The pinned tools in `tools.properties`, read from [properties]: for each tool its version and, per host, the
 * archive's url, SHA-256 and program.
 */
class ToolPins(private val properties: Properties) {
    /** The pin of [tool] for [host], or null when none is pinned for it. */
    fun pin(tool: String, host: String = HostPlatform.current): ToolPin? {
        val version = properties.getProperty("$tool.$VERSION") ?: return null
        fun value(key: String) = properties.getProperty("$tool.$host.$key")
        return ToolPin(tool, version, value(URL) ?: return null, value(SHA256) ?: return null, value(PROGRAM) ?: return null)
    }

    /** The version of [tool], or null when it is not pinned. */
    fun version(tool: String): String? = properties.getProperty("$tool.$VERSION")

    companion object {
        const val NODE = "node"
        private const val VERSION = "version"
        private const val URL = "url"
        private const val SHA256 = "sha256"
        private const val PROGRAM = "program"
        private const val RESOURCE = "/dev/chieppa/arc/plugins/runtime/tools.properties"

        /** The pins Arc ships with. */
        fun bundled(): ToolPins = ToolPins(
            Properties().apply { ToolPins::class.java.getResourceAsStream(RESOURCE)?.use { load(it) } },
        )
    }
}
