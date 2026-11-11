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

/**
 * Watches one file through the file system's watch service on its folder alone, not the folders under it, so watching a
 * file in a large folder costs no more than the folder's own entries.
 */
object NioFileWatcher : FileWatcher {
    override fun changes(file: Path): Flow<Unit> = callbackFlow {
        val folder = file.toAbsolutePath().parent
        Files.createDirectories(folder)
        val service = FileSystems.getDefault().newWatchService()
        folder.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)
        try {
            while (isActive) {
                val key = try {
                    runInterruptible { service.take() }
                } catch (_: ClosedWatchServiceException) {
                    break
                }
                val touched = key.pollEvents().any { (it.context() as? Path)?.toString() == file.fileName.toString() }
                key.reset()
                if (touched) trySend(Unit)
            }
        } finally {
            service.close()
        }
    }.flowOn(Dispatchers.IO)
}
