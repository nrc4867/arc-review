package dev.chieppa.arc.core.actions.timing

/** Percentiles of a handful of numbers, by nearest rank. */
object Percentiles {
    const val P90 = 0.9

    /** The [fraction] percentile of [values], or null for none. */
    fun of(values: List<Long>, fraction: Double = P90): Long? {
        if (values.isEmpty()) return null
        val sorted = values.sorted()
        val rank = kotlin.math.ceil(fraction * sorted.size).toInt().coerceIn(1, sorted.size)
        return sorted[rank - 1]
    }
}
