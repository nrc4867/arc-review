package dev.chieppa.arc.plugins.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runInterruptible
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService

/**
 * Watches a folder and every folder under it, except `node_modules` and dot folders, through the file system's watch
 * service, and says when anything in them is created, changed or deleted. A folder created later is watched too.
 */
object NioDirectoryWatcher : DirectoryWatcher {
    private val SKIPPED = setOf("node_modules")

    override fun changes(folder: Path): Flow<Unit> = callbackFlow {
        val service = FileSystems.getDefault().newWatchService()
        register(service, folder)
        try {
            while (isActive) {
                val key = try {
                    runInterruptible { service.take() }
                } catch (_: ClosedWatchServiceException) {
                    break
                }
                val dir = key.watchable() as Path
                key.pollEvents().forEach { event ->
                    val changed = dir.resolve(event.context() as Path)
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(changed)) register(service, changed)
                }
                key.reset()
                trySend(Unit)
            }
        } finally {
            service.close()
        }
    }.flowOn(Dispatchers.IO)

    private fun register(service: WatchService, root: Path) {
        if (!Files.isDirectory(root)) return
        Files.walk(root).use { paths ->
            paths.filter { Files.isDirectory(it) && watched(root, it) }.forEach {
                it.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)
            }
        }
    }

    private fun watched(root: Path, dir: Path): Boolean =
        root.relativize(dir).none { part -> part.toString() in SKIPPED || part.toString().startsWith(".") }
}
