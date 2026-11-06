package dev.chieppa.arc.store.review

import dev.chieppa.arc.store.files.StoreJson
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

/** Keeps each review in its own JSON file in [dir], named by its id. */
class ReviewStore(private val dir: Path) {
    /** Every stored review that reads, by name. */
    fun all(): List<StoredReview> = ids().mapNotNull(::load).sortedBy { it.name.lowercase() }

    fun load(id: String): StoredReview? = StoreJson.load(file(id), StoredReview.serializer())

    fun save(review: StoredReview) {
        StoreJson.save(file(review.id), StoredReview.serializer(), review)
    }

    fun delete(id: String) {
        Files.deleteIfExists(file(id))
    }

    private fun ids(): List<String> = try {
        Files.list(dir).use { files -> files.filter { it.extension == EXTENSION }.map { it.nameWithoutExtension }.toList() }
    } catch (_: IOException) {
        emptyList()
    }

    private fun file(id: String): Path {
        require(id.isNotBlank() && id.all { it.isLetterOrDigit() || it == '-' }) { "Not a review id: $id" }
        return dir.resolve("$id.$EXTENSION")
    }

    private companion object {
        const val EXTENSION = "json"
    }
}
