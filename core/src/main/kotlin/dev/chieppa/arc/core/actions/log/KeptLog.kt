package dev.chieppa.arc.core.actions.log

/**
 * What is kept of a finished job's log: the [lines] worth keeping in order, how many lines and bytes the whole log had,
 * and its longest silences.
 */
data class KeptLog(
    val lines: List<KeptLine> = emptyList(),
    val totalLines: Int = 0,
    val totalBytes: Long = 0,
    val silences: List<LogSilence> = emptyList(),
) {
    /** About how many bytes are kept. */
    val keptBytes: Long get() = lines.sumOf { it.text.toByteArray(Charsets.UTF_8).size + 1L }

    /** The lines of step [number]. */
    fun step(number: Int): List<KeptLine> = lines.filter { it.step == number }
}
