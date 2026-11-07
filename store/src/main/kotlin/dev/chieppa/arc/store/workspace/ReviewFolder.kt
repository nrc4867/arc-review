package dev.chieppa.arc.store.workspace

import dev.chieppa.arc.store.files.StoreJson
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.nameWithoutExtension

/**
 * The saved reviews in [dir], one JSON file each named by its id, that a workspace starts from when there is no
 * workspace file. Each reads as a workspace; the folder is only read.
 */
class ReviewFolder(private val dir: Path) {
    /** The review [id] when it reads, else the most recently saved review that reads; null when none does. */
    fun startFrom(id: String?): StoredWorkspace? = id?.takeIf(::isId)?.let { load(file(it)) } ?: newestFirst().firstNotNullOfOrNull(::load)

    private fun load(file: Path): StoredWorkspace? = StoreJson.load(file, StoredWorkspace.serializer())

    private fun newestFirst(): List<Path> = try {
        Files.list(dir).use { files -> files.filter { it.extension == EXTENSION && isId(it.nameWithoutExtension) }.toList() }
            .sortedByDescending { it.getLastModifiedTime() }
    } catch (_: IOException) {
        emptyList()
    }

    private fun isId(id: String): Boolean = id.isNotBlank() && id.all { it.isLetterOrDigit() || it == '-' }

    private fun file(id: String): Path = dir.resolve("$id.$EXTENSION")

    private companion object {
        const val EXTENSION = "json"
    }
}
