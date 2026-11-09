package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.log.KeptLine
import dev.chieppa.arc.core.actions.log.LogSilence

/**
 * Kept log lines as console text: GitHub's group markers dimmed, errors in red and warnings in yellow, a dim line
 * where lines were left out saying how many, and the line after a long silence marked with it.
 */
object LogLines {
    private const val DIM = "\u001B[2m"
    private const val RED = "\u001B[31m"
    private const val YELLOW = "\u001B[33m"
    private const val RESET = "\u001B[0m"
    private const val MARKER = "##["
    private const val ERROR = "##[error]"
    private const val WARNING = "##[warning]"

    /** The console text of [lines], with [folded] wording a gap of so many lines and [silent] a silence of so long. */
    fun text(lines: List<KeptLine>, silences: List<LogSilence>, folded: (Int) -> String, silent: (String) -> String): String = buildString {
        val quiet = silences.associate { it.afterIndex to it.seconds }
        var last: Int? = null
        for (line in lines) {
            val gap = last?.let { line.index - it - 1 } ?: 0
            if (gap > 0) append(DIM).append(folded(gap)).append(RESET).append('\n')
            append(colored(line.text))
            quiet[line.index]?.let { append("   ").append(YELLOW).append(silent(RunDurations.of(it))).append(RESET) }
            append('\n')
            last = line.index
        }
    }

    private fun colored(text: String): String = when {
        text.startsWith(ERROR) -> RED + text.removePrefix(ERROR) + RESET
        text.startsWith(WARNING) -> YELLOW + text.removePrefix(WARNING) + RESET
        text.startsWith(MARKER) -> DIM + text + RESET
        else -> text
    }
}
