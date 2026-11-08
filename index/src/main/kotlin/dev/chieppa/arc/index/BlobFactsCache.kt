package dev.chieppa.arc.index

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.syntax.facts.FileFacts
import dev.chieppa.arc.syntax.grammar.Grammar
import java.util.concurrent.ConcurrentHashMap

/**
 * What the index read from each blob, by blob id and grammar, shared by every revision and repo, so a file unchanged
 * between revisions is parsed once.
 */
class BlobFactsCache {
    private val facts = ConcurrentHashMap<String, FileFacts>()

    operator fun get(blob: ObjectId, grammar: Grammar): FileFacts? = facts[key(blob, grammar)]

    operator fun set(blob: ObjectId, grammar: Grammar, value: FileFacts) {
        facts[key(blob, grammar)] = value
    }

    val size: Int get() = facts.size

    /** Forgets every blob but those [files] hold. */
    fun retain(files: Sequence<IndexedFile>) {
        val keep = files.map { key(it.blob, it.grammar) }.toSet()
        facts.keys.retainAll(keep)
    }

    private fun key(blob: ObjectId, grammar: Grammar) = "${blob.value}/${grammar.id}"
}
