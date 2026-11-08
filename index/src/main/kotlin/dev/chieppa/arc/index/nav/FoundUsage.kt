package dev.chieppa.arc.index.nav

import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.index.IndexedFile

/** A place that uses a definition: [length] characters at [line], [column] of [file], as [kind], with a [confidence]. */
data class FoundUsage(
    val file: IndexedFile,
    val line: Int,
    val column: Int,
    val length: Int,
    val name: String,
    val kind: String,
    val confidence: Confidence,
)
