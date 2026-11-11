package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.host.HostFiles
import dev.chieppa.arc.plugins.host.LaunchSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

/**
 * How the plugins' Node process starts: [tools]' Node on the host script [hostFiles] writes, under [PermissionFlags]
 * reading the folders it is given and the host's own files, with source maps on so errors name the plugins' own lines,
 * in the neutral folder [workDir] and with the [NodeEnvironment] made from [parent].
 */
class NodeLaunch(
    private val tools: PluginTools,
    private val hostFiles: suspend () -> HostFiles,
    private val workDir: Path,
    private val parent: Map<String, String> = System.getenv(),
) {
    suspend fun spec(readable: Set<Path>): LaunchSpec {
        val node = tools.node()
        val host = hostFiles()
        withContext(Dispatchers.IO) { Files.createDirectories(workDir) }
        val command = listOf(node.path.toString()) + PermissionFlags.of(readable + setOf(host.folder)) + SOURCE_MAPS + QUIET + host.script.toString()
        return LaunchSpec(command, workDir, NodeEnvironment.of(parent, node.path.toAbsolutePath().parent))
    }

    /** A process of its own type checking the plugin in [folder] from [entry], reading only it and the host's files. */
    suspend fun checkSpec(folder: Path, entry: String): LaunchSpec {
        val node = tools.node()
        val host = hostFiles()
        withContext(Dispatchers.IO) { Files.createDirectories(workDir) }
        val command = listOf(node.path.toString()) + PermissionFlags.of(setOf(folder, host.folder)) + QUIET + host.script.toString() + CHECK + folder.toString() + entry
        return LaunchSpec(command, workDir, NodeEnvironment.of(parent, node.path.toAbsolutePath().parent))
    }

    private companion object {
        const val SOURCE_MAPS = "--enable-source-maps"

        /** The host's flag to type check one plugin, as `TypeCheck.ts` names it. */
        const val CHECK = "--check"

        // Node warns on every thread that --allow-worker widens what may run; Arc grants it knowingly, for its own host.
        const val QUIET = "--disable-warning=PERM0006"
    }
}
