package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The text from [start] up to [end], end excluded. */
@Serializable
data class CodeRange(val start: CodePosition, val end: CodePosition) {
    operator fun contains(position: CodePosition): Boolean = position >= start && position < end

    companion object {
        /** The [length] characters from [line], [column]. */
        fun onLine(line: Int, column: Int, length: Int) = CodeRange(CodePosition(line, column), CodePosition(line, column + length))
    }
}
