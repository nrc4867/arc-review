package dev.chieppa.arc.ui.switcher

/**
 * How a row is picked: Enter switches to it, Ctrl Enter opens it in a new pane, Alt Enter opens it in the strip without
 * switching from the active source.
 */
enum class SwitcherPick { Switch, NewPane, Beside }
