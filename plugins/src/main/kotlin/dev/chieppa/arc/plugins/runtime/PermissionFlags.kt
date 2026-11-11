package dev.chieppa.arc.plugins.runtime

import dev.chieppa.arc.plugins.manifest.Capabilities
import java.nio.file.Path

/**
 * The Node permission model flags a plugin's process starts with: reading only the plugin's own folder, the folders
 * in [readable] (the host script and the bundle), and what its [Capabilities] declare; writing only what they declare;
 * starting programs and using the network only when declared. Node before [NET_MAJOR] cannot limit the network, so
 * there it is left open.
 */
object PermissionFlags {
    const val NET_MAJOR = 25
    private const val PERMISSION = "--permission"
    private const val READ = "--allow-fs-read="
    private const val WRITE = "--allow-fs-write="
    private const val CHILD_PROCESS = "--allow-child-process"
    private const val NET = "--allow-net"

    fun of(capabilities: Capabilities, folder: Path, readable: List<Path>, nodeMajor: Int): List<String> = buildList {
        add(PERMISSION)
        (listOf(folder) + readable + capabilities.files.read.map { folder.resolve(it) }).map { it.normalize() }.distinct().forEach { add("$READ$it") }
        capabilities.files.write.map { folder.resolve(it).normalize() }.distinct().forEach { add("$WRITE$it") }
        if (capabilities.processes) add(CHILD_PROCESS)
        if (capabilities.network.isNotEmpty() && nodeMajor >= NET_MAJOR) add(NET)
    }
}
