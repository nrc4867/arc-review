package dev.chieppa.arc.index.facts

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.syntax.facts.FileFacts
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.FileTime
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Facts as files under [folder]: `<version>/<first two hex digits>/<blob>-<grammar>.facts`, [version] naming the
 * build of fact extraction, so another version's files are never read and are deleted when the store opens. Each read
 * marks its file used now ([clock]); once the files pass [maxBytes], the least recently used are deleted down to
 * [FactsStoreLimits.TRIM_SHARE] of it. A file that cannot be read is deleted and parsed again.
 */
class DiskFactsStore(
    private val folder: Path,
    private val version: String,
    private val maxBytes: Long = FactsStoreLimits.MAX_BYTES,
    private val clock: () -> Long = System::currentTimeMillis,
) : FactsStore {
    private val root = folder.resolve(version)
    private var bytes = -1L

    override fun read(blob: ObjectId, grammar: String): FileFacts? {
        val file = file(blob, grammar)
        val data = try {
            Files.readAllBytes(file)
        } catch (_: IOException) {
            return null
        }
        return try {
            FactsCodec.decode(data).also { touch(file) }
        } catch (_: IOException) {
            delete(file)
            null
        }
    }

    override fun write(blob: ObjectId, grammar: String, facts: FileFacts) {
        val file = file(blob, grammar)
        val data = FactsCodec.encode(facts)
        synchronized(this) { opened() }
        try {
            Files.createDirectories(file.parent)
            val temp = Files.createTempFile(file.parent, file.name, PART)
            Files.write(temp, data)
            Files.setLastModifiedTime(temp, FileTime.fromMillis(clock()))
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: IOException) {
            return
        }
        synchronized(this) {
            bytes += data.size
            if (bytes > maxBytes) trim()
        }
    }

    /** The bytes the store's files take, counted once when first needed, after deleting other versions' files. */
    private fun opened(): Long {
        if (bytes >= 0) return bytes
        if (folder.isDirectory()) folder.listDirectoryEntries().filter { it.name != version }.forEach(::deleteTree)
        bytes = files().sumOf { Files.size(it) }
        return bytes
    }

    private fun trim() {
        val target = (maxBytes * FactsStoreLimits.TRIM_SHARE).toLong()
        val kept = files().map { Kept(it, Files.size(it), Files.getLastModifiedTime(it).toMillis()) }
        bytes = kept.sumOf { it.size }
        for (file in kept.sortedBy { it.used }) {
            if (bytes <= target) break
            if (delete(file.path)) bytes -= file.size
        }
    }

    private fun files(): List<Path> =
        if (!root.isDirectory()) emptyList() else Files.walk(root).use { walk -> walk.filter { Files.isRegularFile(it) }.toList() }

    private fun file(blob: ObjectId, grammar: String): Path =
        root.resolve(blob.value.take(FAN_OUT)).resolve("${blob.value}-$grammar$SUFFIX")

    private fun touch(file: Path) {
        try {
            Files.setLastModifiedTime(file, FileTime.fromMillis(clock()))
        } catch (_: IOException) {
            // A file another process just trimmed is parsed again next time.
        }
    }

    private fun delete(file: Path): Boolean = try {
        Files.deleteIfExists(file)
    } catch (_: IOException) {
        false
    }

    private fun deleteTree(path: Path) {
        Files.walk(path).use { walk -> walk.sorted(Comparator.reverseOrder()).forEach { delete(it) } }
    }

    /** A kept file, its size and when it was last used. */
    private class Kept(val path: Path, val size: Long, val used: Long)

    private companion object {
        const val FAN_OUT = 2
        const val SUFFIX = ".facts"
        const val PART = ".part"
    }
}
