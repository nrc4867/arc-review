package dev.chieppa.arc.ui.review

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.stretch.frame.PaneLayout

/**
 * How one source was left, so switching back to it loses nothing: its pane [layout], the tree rows open under it,
 * and the one [commit] the tree is scoped to, or null for the whole source.
 */
data class SourceView(
    val layout: PaneLayout = PaneLayout(),
    val expanded: Set<String> = emptySet(),
    val commit: ObjectId? = null,
)
