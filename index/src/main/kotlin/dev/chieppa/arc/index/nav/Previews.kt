package dev.chieppa.arc.index.nav

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.index.IndexLimits
import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.index.store.BlobStore
import dev.chieppa.arc.index.store.BlobText

/** The text of lines answers show beside their places, read from the files' blobs, the latest few kept. */
class Previews(private val store: BlobStore) {
    private val texts = object : LinkedHashMap<ObjectId, List<String>>(IndexLimits.PREVIEW_TEXTS, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ObjectId, List<String>>) = size > IndexLimits.PREVIEW_TEXTS
    }

    /** Reads every file of [files] not at hand, one stream per repo. */
    suspend fun load(files: Collection<IndexedFile>) {
        val missing = synchronized(texts) { files.filter { it.blob !in texts } }
        for ((repo, inRepo) in missing.groupBy { it.repo }) {
            store.readBlobs(repo, inRepo.map { it.blob }.distinct()) { id, bytes ->
                val lines = bytes?.let(BlobText::decode)?.lines().orEmpty()
                synchronized(texts) { texts[id] = lines }
            }
        }
    }

    /** [line] (from 1) of [file], without its line break; empty when not loaded. */
    fun line(file: IndexedFile, line: Int): String = synchronized(texts) { texts[file.blob] }?.getOrNull(line - 1)?.trimEnd().orEmpty()

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
