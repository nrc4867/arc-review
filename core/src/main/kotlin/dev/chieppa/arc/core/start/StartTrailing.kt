package dev.chieppa.arc.core.start

/** The one item at the end of a start row. */
sealed interface StartTrailing {
    /** A number, such as how many threads wait. */
    data class Count(val count: Int) : StartTrailing

    /** A short label in a chip of [tone]. */
    data class Chip(val text: String, val tone: StartTone = StartTone.Plain) : StartTrailing

    /** When something happened, in epoch seconds, shown as an age. */
    data class Time(val epochSeconds: Long) : StartTrailing

    /** The keys bound to the action [action], as the keymap has them now. */
    data class Keys(val action: String) : StartTrailing
}
