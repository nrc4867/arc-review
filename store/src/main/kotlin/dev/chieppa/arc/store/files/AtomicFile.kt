package dev.chieppa.arc.store.files

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** Reading and writing a whole file so that a crash mid-write leaves the old one in place. */
object AtomicFile {
    private const val TEMP_SUFFIX = ".tmp"

    /** Writes [text] to a new file beside [path] and moves it into place. */
    fun write(path: Path, text: String) {
        path.toAbsolutePath().parent?.let(Files::createDirectories)
        val temp = path.resolveSibling(path.fileName.toString() + TEMP_SUFFIX)
        Files.writeString(temp, text)
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }

    /** The text of [path], or null when it is missing or unreadable. */
    fun read(path: Path): String? = try {
        Files.readString(path)
    } catch (_: IOException) {
        null
    }
}
