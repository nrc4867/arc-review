package dev.chieppa.arc.index.symbols

import dev.chieppa.arc.index.IndexedFile
import dev.chieppa.arc.syntax.facts.Definition

/** A [definition] in a [file]. */
data class DefSite(val file: IndexedFile, val definition: Definition) {
    fun sameAs(other: DefSite): Boolean = file.sameFile(other.file) && definition.nameStart == other.definition.nameStart
}
