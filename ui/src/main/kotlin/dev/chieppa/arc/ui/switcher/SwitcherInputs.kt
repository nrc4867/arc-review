package dev.chieppa.arc.ui.switcher

/**
 * What the switcher searches: the strip's [sources], their changed [files], the active source's other files at its
 * head, [headFiles], and what followed repos hold that the strip does not, [followed].
 */
data class SwitcherInputs(
    val sources: List<SwitcherItem.InWorkspace> = emptyList(),
    val files: List<SwitcherItem.Changed> = emptyList(),
    val headFiles: List<SwitcherItem.AtHead> = emptyList(),
    val followed: List<SwitcherItem> = emptyList(),
)
