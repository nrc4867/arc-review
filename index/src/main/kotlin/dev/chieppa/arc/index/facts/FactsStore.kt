package dev.chieppa.arc.index.facts

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.syntax.facts.FileFacts

/** Facts kept beyond the process, by blob id and grammar id, so a blob is parsed once. */
interface FactsStore {
    /** The facts kept for [blob] read as [grammar], or null. */
    fun read(blob: ObjectId, grammar: String): FileFacts?

    fun write(blob: ObjectId, grammar: String, facts: FileFacts)

    companion object {
        /** Keeps nothing. */
        val NONE: FactsStore = object : FactsStore {
            override fun read(blob: ObjectId, grammar: String): FileFacts? = null

            override fun write(blob: ObjectId, grammar: String, facts: FileFacts) = Unit
        }
    }
}
