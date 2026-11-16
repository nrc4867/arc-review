package dev.chieppa.arc.ui.switcher

import androidx.compose.ui.input.key.KeyEvent
import dev.chieppa.stretch.core.KeyChord
import dev.chieppa.stretch.frame.chord

/** The switcher's own keys. */
object SwitcherKeys {
    val SWITCH: KeyChord = KeyChord.parse("Enter")
    val NEW_PANE: KeyChord = KeyChord.parse("Ctrl Enter")
    val BESIDE: KeyChord = KeyChord.parse("Alt Enter")
    val SCOPE: KeyChord = KeyChord.parse("Tab")
    val SCOPE_BACK: KeyChord = KeyChord.parse("Shift Tab")
    val DOWN: KeyChord = KeyChord.parse("Down")
    val UP: KeyChord = KeyChord.parse("Up")
    val CLOSE: KeyChord = KeyChord.parse("Esc")

    /** How [event] picks the highlighted row, or null when it picks none. */
    fun pickOf(event: KeyEvent): SwitcherPick? = when (event.chord()) {
        SWITCH -> SwitcherPick.Switch
        NEW_PANE -> SwitcherPick.NewPane
        BESIDE -> SwitcherPick.Beside
        else -> null
    }
}
