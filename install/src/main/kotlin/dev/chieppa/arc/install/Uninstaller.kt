package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.Path

/**
 * Takes the installation out of [layout]: the [launcher], the copied jars, `launch.args`, the icons and the installed
 * runtimes. The person's data in the data folder stays.
 */
class Uninstaller(private val layout: InstallLayout, private val launcher: Launcher) {
    /** Removes everything it can and gives what it could not, such as a jar or runtime a running Arc holds open. */
    fun uninstall(): List<Path> {
        val left = mutableListOf<Path>()
        runCatching { launcher.remove() }.onFailure { left.add(layout.app) }
        val runtimes = if (Files.isDirectory(layout.runtimes)) {
            Files.list(layout.runtimes).use { it.toList() }.filter { it.fileName.toString().startsWith(InstallConstants.RUNTIME_TOOL + "-") }
        } else {
            emptyList()
        }
        (runtimes + listOf(layout.app)).forEach { folder -> runCatching { AtomicFiles.deleteTree(folder) }.onFailure { left.add(folder) } }
        return left.distinct()
    }
}
