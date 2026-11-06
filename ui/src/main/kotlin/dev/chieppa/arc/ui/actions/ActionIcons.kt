package dev.chieppa.arc.ui.actions

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import dev.chieppa.stretch.theme.StretchIcons

/** The icon for an action's icon key: a drawer action shows its drawer's icon. */
fun actionIcon(key: String): ImageVector? = when (key) {
    ArcActionIds.GO_TO_ANYTHING.value -> StretchIcons.Search
    ArcActionIds.SETTINGS.value -> StretchIcons.Settings
    else -> ArcActionIds.drawerOf(key)?.let(ArcDrawerSlots::byId)?.icon
}
