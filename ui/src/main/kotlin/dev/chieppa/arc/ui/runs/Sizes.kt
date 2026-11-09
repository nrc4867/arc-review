package dev.chieppa.arc.ui.runs

/** How file sizes read: `1 KB`, `4.1 MB`. */
object Sizes {
    private const val KB = 1024.0
    private val UNITS = listOf("B", "KB", "MB", "GB")

    fun bytes(count: Long): String {
        var value = count.toDouble()
        var unit = 0
        while (value >= KB && unit < UNITS.lastIndex) {
            value /= KB
            unit++
        }
        val shown = if (unit <= 1 || value >= TEN) value.toLong().toString() else String.format(java.util.Locale.ROOT, "%.1f", value)
        return "$shown ${UNITS[unit]}"
    }

    private const val TEN = 10
}
