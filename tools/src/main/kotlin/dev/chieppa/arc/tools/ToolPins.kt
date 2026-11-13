package dev.chieppa.arc.tools

import java.util.Properties

/**
 * Pinned tools read from [properties]: for each tool its `<tool>.version` and, per host, the archive's
 * `<tool>.<host>.url`, `sha256` and `program`.
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
        private const val VERSION = "version"
        private const val URL = "url"
        private const val SHA256 = "sha256"
        private const val PROGRAM = "program"

        /** The pins in the resource at absolute [path] on [owner]'s class path; none when it is missing. */
        fun resource(owner: Class<*>, path: String): ToolPins = ToolPins(
            Properties().apply { owner.getResourceAsStream(path)?.use { load(it) } },
        )
    }
}
