package dev.chieppa.arc.plugins.console

/** What a console process has printed: the last [chunks] kept, oldest first, and the [total] it has printed. */
data class ConsolePrinted(val chunks: List<ConsoleChunk> = emptyList(), val total: Long = 0)
