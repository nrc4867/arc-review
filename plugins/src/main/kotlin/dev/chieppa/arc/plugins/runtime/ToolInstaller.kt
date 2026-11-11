package dev.chieppa.arc.plugins.runtime

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Keeps pinned tools under [root], one folder per tool and version: the first time one is asked for, its archive is
 * downloaded through [downloader], checked against its SHA-256 and unpacked, and the folder appears only once complete,
 * so an interrupted install is never used.
 */
class ToolInstaller(private val root: Path, private val downloader: Downloader) {
    private val lock = Mutex()

    /** The program [pin] names, installed first if needed. Throws [IOException] when it cannot be. */
    suspend fun install(pin: ToolPin): Path = lock.withLock {
        val home = root.resolve(pin.folderName)
        val program = home.resolve(pin.program)
        if (Files.isRegularFile(program)) return program
        withContext(Dispatchers.IO) { Files.createDirectories(root) }
        val work = withContext(Dispatchers.IO) { Files.createTempDirectory(root, WORK_PREFIX) }
        try {
            val archive = work.resolve(pin.url.substringAfterLast('/'))
            downloader.download(pin.url, pin.sha256, archive)
            withContext(Dispatchers.IO) {
                val unpacked = work.resolve(UNPACKED)
                ArchiveUnpacker.unpack(archive, pin.isZip, unpacked)
                if (!Files.isRegularFile(unpacked.resolve(pin.program))) throw IOException("${pin.url} does not hold ${pin.program}")
                if (!Files.exists(home)) Files.move(unpacked, home, StandardCopyOption.ATOMIC_MOVE)
            }
        } finally {
            withContext(Dispatchers.IO) { work.toFile().deleteRecursively() }
        }
        program
    }

    /** Whether [pin] is installed already. */
    fun installed(pin: ToolPin): Boolean = Files.isRegularFile(root.resolve(pin.folderName).resolve(pin.program))

    private companion object {
        const val WORK_PREFIX = ".install-"
        const val UNPACKED = "unpacked"
    }
}
