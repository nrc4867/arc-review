package dev.chieppa.arc.ui.switcher

/** What the switcher searches: everything, only sources (the review's and followed repos'), or only files. */
enum class SwitcherScope {
    All,
    Sources,
    Files,
    ;

    /** The scope Tab moves to, or Shift Tab with [back]. */
    fun next(back: Boolean = false): SwitcherScope = entries[(ordinal + if (back) entries.size - 1 else 1) % entries.size]
}
