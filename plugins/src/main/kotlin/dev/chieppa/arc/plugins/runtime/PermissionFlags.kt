package dev.chieppa.arc.plugins.runtime

import java.nio.file.Path

/**
 * The Node permission flags the plugins' process starts with, the same for every plugin: reading only the folders in
 * [of]'s list (the plugins' sources and the host's own files) and starting worker threads. Writing files, starting
 * programs, the network, native addons and WASI stay denied; a plugin does those through the app object, which Arc
 * checks against its manifest.
 */
object PermissionFlags {
    private const val PERMISSION = "--permission"
    private const val READ = "--allow-fs-read="
    private const val WORKER = "--allow-worker"

    fun of(readable: Collection<Path>): List<String> = buildList {
        add(PERMISSION)
        readable.map { it.toAbsolutePath().normalize() }.distinct().sorted().forEach { add("$READ$it") }
        add(WORKER)
    }
}
