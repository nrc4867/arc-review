package dev.chieppa.arc.ui.runs

/** How run and step times read: `41s`, `6m 10s`, `1h 05m`. */
object RunDurations {
    private const val MINUTE = 60L
    private const val HOUR = 60 * MINUTE

    fun of(seconds: Long): String = when {
        seconds < MINUTE -> "${seconds}s"
        seconds < HOUR -> "${seconds / MINUTE}m ${pad(seconds % MINUTE)}s"
        else -> "${seconds / HOUR}h ${pad((seconds % HOUR) / MINUTE)}m"
    }

    /** Minutes only, for a time over usual: `9m`, or seconds under a minute. */
    fun minutes(seconds: Long): String = if (seconds < MINUTE) "${seconds}s" else "${seconds / MINUTE}m"

    private fun pad(value: Long): String = value.toString().padStart(2, '0')
}
