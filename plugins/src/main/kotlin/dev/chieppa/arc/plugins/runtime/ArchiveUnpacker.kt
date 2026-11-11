package dev.chieppa.arc.plugins.runtime

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

/**
 * Unpacks a zip or a gzipped tar into a folder: regular files and folders only, links skipped, and nothing written
 * outside the folder. A tar entry marked executable stays executable where the file system keeps permissions.
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
        TarReader(GZIPInputStream(Files.newInputStream(archive))).use { tar ->
            while (true) {
                val entry = tar.next() ?: break
                val target = inside(into, entry.name)
                when {
                    entry.directory -> Files.createDirectories(target)
                    entry.file -> write(tar.content(), target, entry.executable)
                }
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
