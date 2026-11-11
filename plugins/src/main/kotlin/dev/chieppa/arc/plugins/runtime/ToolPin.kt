package dev.chieppa.arc.plugins.runtime

/**
 * One pinned archive of a tool: where it is, the SHA-256 it must have, and the [program] inside it, a path relative to
 * where it unpacks. [version] is the tool's.
 */
data class ToolPin(val tool: String, val version: String, val url: String, val sha256: String, val program: String) {
    /** Whether the archive is a zip; otherwise it is a gzipped tar. */
    val isZip: Boolean get() = url.endsWith(ZIP)

    /** The folder this pin installs into, named by tool and version. */
    val folderName: String get() = "$tool-$version"

    private companion object {
        const val ZIP = ".zip"
    }
}
