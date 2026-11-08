package dev.chieppa.arc.ui.navigation

import androidx.compose.ui.geometry.Rect
import dev.chieppa.arc.codeintel.model.SourceScope
import dev.chieppa.arc.codeintel.router.Navigation

/**
 * A list a Ctrl+click opened: [navigation] (candidates or usages) over the pane [paneId], under the name clicked at
 * [anchor], with the [sources] it was asked across.
 */
data class NavigationPopup(val paneId: String, val anchor: Rect?, val navigation: Navigation, val sources: List<SourceScope>)
