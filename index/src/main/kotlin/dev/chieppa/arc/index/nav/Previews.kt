package dev.chieppa.arc.index.nav

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.index.IndexLimits
import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.index.store.BlobStore
import dev.chieppa.arc.index.store.BlobText

/** The text of files answers show from, read from their blobs, the latest few kept. */
class Previews(private val store: BlobStore) {
    private class Loaded(val text: String) {
        val lines: List<String> by lazy { text.lines() }
    }

    private val texts = object : LinkedHashMap<ObjectId, Loaded>(IndexLimits.PREVIEW_TEXTS, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ObjectId, Loaded>) = size > IndexLimits.PREVIEW_TEXTS
    }

    /** Reads every file of [files] not at hand, one stream per repo. */
    suspend fun load(files: Collection<IndexedFile>) {
        val missing = synchronized(texts) { files.filter { it.blob !in texts } }
        for ((repo, inRepo) in missing.groupBy { it.repo }) {
            store.readBlobs(repo, inRepo.map { it.blob }.distinct()) { id, bytes ->
                val text = bytes?.let(BlobText::decode).orEmpty()
                synchronized(texts) { texts[id] = Loaded(text) }
            }
        }
    }

    /** [line] (from 1) of [file], without its line break; empty when not loaded. */
    fun line(file: IndexedFile, line: Int): String = synchronized(texts) { texts[file.blob] }?.lines?.getOrNull(line - 1)?.trimEnd().orEmpty()

    /** The whole text of [file], as the index parsed it; null when not loaded. */
    fun text(file: IndexedFile): String? = synchronized(texts) { texts[file.blob] }?.text

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
