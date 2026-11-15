package dev.chieppa.arc.design

import androidx.compose.ui.graphics.Color
import dev.chieppa.stretch.theme.StretchTheme
import dev.chieppa.stretch.theme.StretchThemes
import dev.chieppa.stretch.theme.contrastRatio
import kotlin.test.Test
import kotlin.test.assertTrue

/** Arc's own colors read beside Stretch's in every theme, held to the same pairs Stretch's tokens are. */
class ArcColorsContrastTest {
    private class Check(val name: String, val fg: Color, val bg: Color, val min: Double)

    @Test
    fun arcColorsReadInEveryTheme() {
        val failures = StretchThemes.all.flatMap { theme -> pairs(theme).filter { contrastRatio(it.fg, it.bg) < it.min }.map { "${theme.id}: ${it.name}" } }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    private fun pairs(theme: StretchTheme): List<Check> {
        val c = theme.colors
        val arc = ArcColorSets.forDark(theme.isDark)
        val grounds = listOf(c.surfaceCanvas, c.surfaceRail, c.surfacePanel, c.surfaceFloat, c.surfaceControl, c.surfaceControlHi)
        val inks = listOf("guest" to arc.guest, "addressed" to arc.addressed)
        val onGrounds = inks.flatMap { (name, ink) -> grounds.map { Check("$name on a surface", ink, it, BODY) } }
        val onWashes = listOf(Check("guest on its wash", arc.guest, arc.guestWash, BODY), Check("addressed on its wash", arc.addressed, arc.addressedWash, BODY))
        val bars = listOf(Check("a passed job's edge", arc.passEdge, c.surfacePanel, GRAPHIC))
        // A light theme holds what is read at length to 7:1, as Stretch's light themes hold their secondary text.
        val reading = if (theme.isDark) emptyList() else inks.flatMap { (name, ink) ->
            listOf(c.surfacePanel, c.surfaceFloat).map { Check("$name read on a light panel", ink, it, LIGHT_READING) }
        }
        return onGrounds + onWashes + bars + reading
    }

    private companion object {
        const val BODY = 4.5
        const val GRAPHIC = 3.0
        const val LIGHT_READING = 7.0
    }
}
