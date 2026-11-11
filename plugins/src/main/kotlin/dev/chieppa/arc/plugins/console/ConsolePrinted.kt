package dev.chieppa.arc.plugins.console

/**
 * What a console process has printed: the pieces making up its last lines, oldest first, each holding at most one line
 * and the last perhaps a line still being written, and the [total] number of pieces it has printed.
 */
data class ConsolePrinted(val chunks: List<ConsoleChunk> = emptyList(), val total: Long = 0)
