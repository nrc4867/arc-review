package dev.chieppa.arc.ui.settings

import kotlin.math.abs
import kotlin.math.roundToInt

/** The interface sizes Arc offers, 1 being normal. */
object InterfaceSizes {
    const val DEFAULT = 1f
    private const val PERCENT = 100

    val all: List<Float> = listOf(0.9f, 1f, 1.25f, 1.5f)

    /** The offered size nearest [size], or the default when there is none. */
    fun nearest(size: Float?): Float = size?.let { wanted -> all.minBy { abs(it - wanted) } } ?: DEFAULT

    /** [size] as a whole percentage. */
    fun percent(size: Float): Int = (size * PERCENT).roundToInt()

    /** The id a setting's choice gives [size]: its percentage. */
    fun id(size: Float): String = percent(size).toString()

    /** The offered size with [id], or null when none has it. */
    fun byId(id: String): Float? = all.firstOrNull { id(it) == id }
}
