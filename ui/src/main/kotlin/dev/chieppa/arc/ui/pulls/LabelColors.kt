package dev.chieppa.arc.ui.pulls

import androidx.compose.ui.graphics.Color

/** A label's color as GitHub gives it, six hex digits. */
object LabelColors {
    private const val HEX_DIGITS = 6
    private const val RADIX = 16
    private const val OPAQUE = 0xFF000000

    /** [hex] as a color, or [fallback] when it is not six hex digits. */
    fun of(hex: String, fallback: Color): Color {
        val digits = hex.removePrefix("#")
        if (digits.length != HEX_DIGITS) return fallback
        val value = digits.toLongOrNull(RADIX) ?: return fallback
        return Color(OPAQUE or value)
    }
}
