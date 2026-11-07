package dev.chieppa.arc.ui.actions

import dev.chieppa.stretch.core.KeyChord

/** Arc's default shortcuts. Ctrl is Cmd on macOS. */
object ArcKeys {
    val GO_TO_ANYTHING: KeyChord = KeyChord.parse("Ctrl K")
    val COMMANDS: KeyChord = KeyChord.parse("Ctrl Shift P")
    val NEXT_FILE: KeyChord = KeyChord.parse("Alt Down")
    val PREVIOUS_FILE: KeyChord = KeyChord.parse("Alt Up")
    val NEXT_CHANGE: KeyChord = KeyChord.parse("F7")
    val PREVIOUS_CHANGE: KeyChord = KeyChord.parse("Shift F7")
    val MARK_AND_ADVANCE: KeyChord = KeyChord.parse("R")
    val TOGGLE_REVIEWED: KeyChord = KeyChord.parse("Shift R")
    val FIND_IN_FILE: KeyChord = KeyChord.parse("Ctrl F")
    val OPEN_FILE: KeyChord = KeyChord.parse("Ctrl P")
    val CLOSE_PANE: KeyChord = KeyChord.parse("Ctrl W")
    val TOGGLE_WRAP: KeyChord = KeyChord.parse("Alt Z")

    /** The keys that act on the file tree's selection, so the tree must not take them as typing. */
    val REVIEW_KEYS: Set<KeyChord> = setOf(MARK_AND_ADVANCE, TOGGLE_REVIEWED)
}
