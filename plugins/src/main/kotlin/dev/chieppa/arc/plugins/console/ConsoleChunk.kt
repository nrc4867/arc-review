package dev.chieppa.arc.plugins.console

/** Some [text] a console process printed, on standard error when [error]. */
data class ConsoleChunk(val text: String, val error: Boolean = false)
