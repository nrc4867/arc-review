package dev.chieppa.arc.ui.switcher

import androidx.compose.ui.input.key.KeyEvent
import dev.chieppa.stretch.core.KeyChord
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.frame.chord

/** The switcher's own keys, and their caps as its footer shows them, Enter drawn as ↵. */
object SwitcherKeys {
    val SWITCH: KeyChord = KeyChord.parse("Enter")
    val NEW_PANE: KeyChord = KeyChord.parse("Ctrl Enter")
    val ADD: KeyChord = KeyChord.parse("Alt Enter")
    val SCOPE: KeyChord = KeyChord.parse("Tab")
    val SCOPE_BACK: KeyChord = KeyChord.parse("Shift Tab")
    val DOWN: KeyChord = KeyChord.parse("Down")
    val UP: KeyChord = KeyChord.parse("Up")
    val CLOSE: KeyChord = KeyChord.parse("Esc")

    private const val ENTER = "Enter"
    private const val ENTER_CAP = "↵"

    /** How [event] picks the highlighted row, or null when it picks none. */
    fun pickOf(event: KeyEvent): SwitcherPick? = when (event.chord()) {
        SWITCH -> SwitcherPick.Switch
        NEW_PANE -> SwitcherPick.NewPane
        ADD -> SwitcherPick.Add
        else -> null
    }

    /** The caps for [chord], as the footer shows them. */
    fun caps(chord: KeyChord): List<String> = chord.caps().map { if (it == ENTER) ENTER_CAP else it }
}
