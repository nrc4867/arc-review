package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** A place in a file's text: [line] from 1, [column] from 0 in UTF-16 code units, as a JVM or JavaScript string counts. */
@Serializable
data class CodePosition(val line: Int, val column: Int) : Comparable<CodePosition> {
    override fun compareTo(other: CodePosition): Int = compareValuesBy(this, other, CodePosition::line, CodePosition::column)
}
