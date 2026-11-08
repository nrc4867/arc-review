package dev.chieppa.arc.host.github.avatars

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Avatar pictures, asked of GitHub at [AvatarUrls.SIZE] and kept in memory and as files in [folder], each named by the
 * SHA-256 of its sized address and fresh for [ttl] from its file's last-modified time. A stale or missing picture is
 * fetched again; when that fails the stale copy is kept. Requests for one address at once share one fetch.
 */
class AvatarCache(
    private val folder: Path,
    private val fetcher: AvatarFetcher = JdkAvatarFetcher(),
    private val ttl: Duration = TTL,
    private val clock: () -> Instant = Instant::now,
) {
    private class Entry(val bytes: ByteArray, val at: Instant)

    private val memory = ConcurrentHashMap<String, Entry>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    /** The picture at [url], or null when there is none here and GitHub gave none. */
    suspend fun bytes(url: String): ByteArray? {
        val sized = AvatarUrls.sized(url)
        memory[sized]?.takeIf(::fresh)?.let { return it.bytes }
        return locks.computeIfAbsent(sized) { Mutex() }.withLock { load(sized) }
    }

    private suspend fun load(sized: String): ByteArray? {
        memory[sized]?.takeIf(::fresh)?.let { return it.bytes }
        val file = folder.resolve(AvatarUrls.fileName(sized))
        val stored = memory[sized] ?: read(file)
        if (stored != null && fresh(stored)) {
            memory[sized] = stored
            return stored.bytes
        }
        val fetched = try {
            fetcher.fetch(sized)
        } catch (_: IOException) {
            null
        }
        if (fetched == null) return stored?.bytes
        val entry = Entry(fetched, clock())
        memory[sized] = entry
        write(file, entry)
        return fetched
    }

    private fun fresh(entry: Entry): Boolean = Duration.between(entry.at, clock()) < ttl

    private suspend fun read(file: Path): Entry? = withContext(Dispatchers.IO) {
        try {
            if (Files.isRegularFile(file)) Entry(Files.readAllBytes(file), Files.getLastModifiedTime(file).toInstant()) else null
        } catch (_: IOException) {
            null
        }
    }

    private suspend fun write(file: Path, entry: Entry): Unit = withContext(Dispatchers.IO) {
        try {
            Files.createDirectories(folder)
            Files.write(file, entry.bytes)
            Files.setLastModifiedTime(file, FileTime.from(entry.at))
        } catch (_: IOException) {
            // The picture stays in memory; the next run fetches it again.
        }
    }

    companion object {
        /** How long a picture is used before it is fetched again. */
        val TTL: Duration = Duration.ofHours(24)
    }
}
