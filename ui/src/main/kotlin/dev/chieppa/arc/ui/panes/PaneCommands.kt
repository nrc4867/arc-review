package dev.chieppa.arc.ui.panes

/**
 * What the window's commands can ask of a pane on screen: moving between its changes, finding in it, and clicking
 * through the name at its caret.
 */
interface PaneCommands {
    /** What the pane is finding, for the status bar. */
    val find: dev.chieppa.arc.ui.find.FindState

    suspend fun nextChange()

    suspend fun previousChange()

    fun openFind()

    /** Moves [step] find matches on, wrapping round, and scrolls to the match. */
    suspend fun moveMatch(step: Int)

    /** Acts on the name at the caret as a Ctrl+click on it would; false when there is no name there. */
    suspend fun clickSymbolAtCaret(): Boolean = false
}
