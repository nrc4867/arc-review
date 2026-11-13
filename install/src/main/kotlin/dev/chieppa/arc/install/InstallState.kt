package dev.chieppa.arc.install

import java.io.IOException
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * What is installed: the [current] version the launcher starts and the [previous] one Settings can switch back to.
 * Kept in `app/install.properties`, its paths relative to the data folder.
 */
data class InstallState(val current: InstalledVersion? = null, val previous: InstalledVersion? = null) {
    /** This state with [version] current and the current one kept as previous, unless they are the same version. */
    fun withCurrent(version: InstalledVersion): InstallState =
        InstallState(version, if (current != null && current.version != version.version) current else previous?.takeIf { it.version != version.version })

    /** This state with current and previous swapped; unchanged when there is no previous. */
    fun switched(): InstallState = if (previous == null) this else InstallState(previous, current)

    /** Writes this state to [layout]'s state file, through a file beside it that moves into place. */
    fun write(layout: InstallLayout) {
        val properties = Properties()
        current?.let { put(properties, CURRENT, it, layout.dataDir) }
        previous?.let { put(properties, PREVIOUS, it, layout.dataDir) }
        val text = StringWriter().also { properties.store(it, null) }.toString()
        AtomicFiles.write(layout.state, text)
    }

    companion object {
        private const val CURRENT = "current"
        private const val PREVIOUS = "previous"
        private const val VERSION = "version"
        private const val JAR = "jar"
        private const val JAVA_HOME = "javaHome"

        /** The state in [layout]'s state file; nothing installed when there is none or it cannot be read. */
        fun read(layout: InstallLayout): InstallState {
            val properties = Properties()
            try {
                Files.newBufferedReader(layout.state).use { properties.load(it) }
            } catch (_: IOException) {
                return InstallState()
            }
            return InstallState(get(properties, CURRENT, layout.dataDir), get(properties, PREVIOUS, layout.dataDir))
        }

        private fun put(properties: Properties, key: String, version: InstalledVersion, dataDir: Path) {
            properties.setProperty("$key.$VERSION", version.version)
            properties.setProperty("$key.$JAR", relative(dataDir, version.jar))
            properties.setProperty("$key.$JAVA_HOME", relative(dataDir, version.javaHome))
        }

        private fun get(properties: Properties, key: String, dataDir: Path): InstalledVersion? {
            val version = properties.getProperty("$key.$VERSION") ?: return null
            val jar = properties.getProperty("$key.$JAR") ?: return null
            val home = properties.getProperty("$key.$JAVA_HOME") ?: return null
            return InstalledVersion(version, dataDir.resolve(jar).normalize(), dataDir.resolve(home).normalize())
        }

        /** [path] relative to [dataDir] with `/` between names, or absolute when it lies outside. */
        private fun relative(dataDir: Path, path: Path): String =
            if (path.startsWith(dataDir)) dataDir.relativize(path).joinToString("/") else path.toString()
    }
}
