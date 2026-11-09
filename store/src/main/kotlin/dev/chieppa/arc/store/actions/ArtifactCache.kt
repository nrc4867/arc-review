package dev.chieppa.arc.store.actions

import dev.chieppa.arc.core.actions.RunRef
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime
import kotlin.io.path.isRegularFile

/**
 * Downloaded artifacts under [folder], at `<owner>/<name>/<artifact id>`, holding at most [capBytes] in all: once a
 * new one goes over, the ones used longest ago go first. Using one marks it used now through [clock].
 */
class ArtifactCache(private val folder: Path, val capBytes: Long = CAP_BYTES, private val clock: () -> Long = System::currentTimeMillis) {
    /** The file of artifact [id] of [run]'s repo, if it is here; it counts as used now. */
    fun get(run: RunRef, id: Long): Path? = file(run, id).takeIf { it.isRegularFile() }?.also(::touch)

    /** Writes artifact [id] from [input] and returns its file, making room by dropping the least recently used. */
    fun put(run: RunRef, id: Long, input: InputStream): Path {
        val target = file(run, id)
        Files.createDirectories(target.parent)
        val temp = target.resolveSibling("${target.fileName}$PARTIAL")
        Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING)
        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
        touch(target)
        trim(target)
        return target
    }

    /** How many bytes the cache holds. */
    fun size(): Long = entries().sumOf { Files.size(it) }

    private fun trim(keep: Path) {
        var total = size()
        if (total <= capBytes) return
        for (path in entries().sortedBy { Files.getLastModifiedTime(it).toMillis() }) {
            if (total <= capBytes) break
            if (path == keep) continue
            total -= Files.size(path)
            Files.deleteIfExists(path)
        }
    }

    private fun entries(): List<Path> {
        if (!Files.isDirectory(folder)) return emptyList()
        return try {
            Files.walk(folder).use { paths -> paths.filter { it.isRegularFile() && !it.toString().endsWith(PARTIAL) }.toList() }
        } catch (_: IOException) {
            emptyList()
        }
    }

    private fun touch(path: Path) {
        Files.setLastModifiedTime(path, FileTime.fromMillis(clock()))
    }

    private fun file(run: RunRef, id: Long): Path {
        val parts = run.repo.lowercase().split('/', limit = 2)
        return folder.resolve(parts[0]).resolve(parts.getOrElse(1) { "" }).resolve(id.toString())
    }

    companion object {
        const val CAP_BYTES = 2L * 1024 * 1024 * 1024
        private const val PARTIAL = ".part"
    }
}
