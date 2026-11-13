package dev.chieppa.arc.install

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/** Files and folders written beside where they go and moved into place, so an interrupted write leaves the old one whole. */
object AtomicFiles {
    /** Writes [text] to [target]. */
    fun write(target: Path, text: String) {
        Files.createDirectories(target.parent)
        val partial = Files.createTempFile(target.parent, InstallConstants.PARTIAL_PREFIX, target.fileName.toString())
        try {
            Files.writeString(partial, text)
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(partial)
        }
    }

    /** Copies [source] to [target]. */
    fun copy(source: Path, target: Path) {
        Files.createDirectories(target.parent)
        val partial = Files.createTempFile(target.parent, InstallConstants.PARTIAL_PREFIX, target.fileName.toString())
        try {
            Files.copy(source, partial, StandardCopyOption.REPLACE_EXISTING)
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            Files.deleteIfExists(partial)
        }
    }

    /**
     * Makes the folder [target] by letting [fill] write a folder beside it, then puts that in its place: the old
     * folder is moved aside first and deleted once the new one is in.
     */
    fun replaceFolder(target: Path, fill: (Path) -> Unit) {
        Files.createDirectories(target.parent)
        val partial = Files.createTempDirectory(target.parent, InstallConstants.PARTIAL_PREFIX)
        val old = target.resolveSibling(InstallConstants.PARTIAL_PREFIX + "old-" + target.fileName)
        try {
            fill(partial)
            deleteTree(old)
            if (Files.exists(target)) Files.move(target, old, StandardCopyOption.ATOMIC_MOVE)
            Files.move(partial, target, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            deleteTree(partial)
            deleteTree(old)
        }
    }

    /** Deletes [path] and everything under it, not following links; nothing when it is not there. */
    fun deleteTree(path: Path) {
        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) return
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            Files.list(path).use { children -> children.toList() }.forEach(::deleteTree)
        }
        Files.delete(path)
    }
}
