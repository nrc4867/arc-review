package dev.chieppa.arc.plugins.manager

import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.LinkOption
import java.nio.file.Path

/** Where a plugin's file calls may reach: under one of the folders its manifest names, links followed. */
object PluginPaths {
    /**
     * The file [path] names (absolute, or relative to the plugin's [folder]) when it lies under one of [declared]
     * (each absolute or relative to [folder]), with every link in it and in the folders followed; null otherwise.
     */
    fun resolve(folder: Path, declared: List<String>, path: String): Path? {
        val target = try {
            folder.resolve(path).toAbsolutePath().normalize()
        } catch (_: InvalidPathException) {
            return null
        }
        val roots = declared.mapNotNull { runCatching { folder.resolve(it).toAbsolutePath().normalize() }.getOrNull() }
        val root = roots.firstOrNull { target.startsWith(it) } ?: return null
        val realRoot = real(root) ?: return null
        val real = real(target) ?: return null
        return real.takeIf { it.startsWith(realRoot) }
    }

    /** [path] with its links followed, its missing tail kept as written; null when it cannot be read. */
    private fun real(path: Path): Path? {
        var existing = path
        while (!Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) existing = existing.parent ?: return null
        return try {
            existing.toRealPath().resolve(existing.relativize(path))
        } catch (_: IOException) {
            null
        }
    }
}
