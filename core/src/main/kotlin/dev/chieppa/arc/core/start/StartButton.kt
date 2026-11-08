package dev.chieppa.arc.core.start

/**
 * A button in the start screen's row of main buttons, under the screen's header. [icon] is a name from Arc's or
 * Stretch's icons; [keys] names the action whose keys show beside the label; [primary] draws it in the on colors.
 */
data class StartButton(
    val label: String,
    val action: StartAction,
    val icon: String? = null,
    val keys: String? = null,
    val primary: Boolean = false,
)
