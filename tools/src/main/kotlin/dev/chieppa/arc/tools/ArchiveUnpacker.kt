package dev.chieppa.arc.tools

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/**
 * Unpacks a zip or a gzipped tar into a folder, writing nothing outside it. A tar entry marked executable stays
 * executable where the file system keeps permissions. A tar's links are made once every file is written: a symbolic
 * link stays a link when its target is inside the folder (a copy of the target where the file system makes no links),
 * a hard link becomes a copy of its target, and a link reaching outside the folder fails the unpacking.
 */
object ArchiveUnpacker {
    fun unpack(archive: Path, zip: Boolean, into: Path) {
        Files.createDirectories(into)
        if (zip) unzip(archive, into) else untar(archive, into)
    }

    private fun unzip(archive: Path, into: Path) {
        ZipInputStream(Files.newInputStream(archive)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val target = inside(into, entry.name)
                if (entry.isDirectory) Files.createDirectories(target) else write(zip, target, executable = false)
            }
        }
    }

    private fun untar(archive: Path, into: Path) {
        val links = mutableListOf<TarReader.Entry>()
        TarReader(GZIPInputStream(Files.newInputStream(archive))).use { tar ->
            while (true) {
                val entry = tar.next() ?: break
                val target = inside(into, entry.name)
                when {
                    entry.link != null -> links += entry
                    entry.directory -> Files.createDirectories(target)
                    entry.file -> write(tar.content(), target, entry.executable)
                }
            }
        }
        links.forEach { link(into, it) }
    }

    private fun link(root: Path, entry: TarReader.Entry) {
        val path = inside(root, entry.name)
        val name = requireNotNull(entry.link)
        val target = if (entry.symbolic) symbolicTarget(root, path, name, entry.name) else inside(root, name)
        path.parent?.let(Files::createDirectories)
        Files.deleteIfExists(path)
        if (entry.symbolic && runCatching { Files.createSymbolicLink(path, Path.of(name)) }.isSuccess) return
        copy(target, path)
    }

    /** Where a symbolic link at [path] to [name] leads, refusing one that is absolute or leaves [root]. */
    private fun symbolicTarget(root: Path, path: Path, name: String, entryName: String): Path {
        if (name.startsWith("/") || Path.of(name).isAbsolute) throw IOException("$entryName links outside the archive's folder")
        val target = path.parent.resolve(name).normalize()
        if (!target.startsWith(root.normalize())) throw IOException("$entryName links outside the archive's folder")
        return target
    }

    private fun copy(from: Path, to: Path) {
        if (!Files.exists(from)) throw IOException("$to links to $from, which the archive does not hold")
        if (!Files.isDirectory(from)) {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING)
            return
        }
        Files.walk(from).use { paths ->
            paths.forEach { path ->
                val copied = to.resolve(from.relativize(path).toString())
                if (Files.isDirectory(path)) Files.createDirectories(copied) else Files.copy(path, copied, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun write(input: InputStream, target: Path, executable: Boolean) {
        target.parent?.let(Files::createDirectories)
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
        if (executable) runCatching { Files.setPosixFilePermissions(target, Files.getPosixFilePermissions(target) + EXECUTE) }
    }

    private fun inside(root: Path, name: String): Path {
        val target = root.resolve(name).normalize()
        if (!target.startsWith(root.normalize())) throw IOException("$name points outside the archive's folder")
        return target
    }

    private val EXECUTE = setOf(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE)
}
