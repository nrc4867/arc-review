package dev.chieppa.arc.ui.actions

import androidx.compose.ui.graphics.vector.ImageVector
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.drawers.ArcDrawerSlots
import dev.chieppa.stretch.theme.StretchIcons

private val icons: Map<String, ImageVector> = mapOf(
    ArcActionIds.GO_TO_ANYTHING.value to StretchIcons.Search,
    ArcActionIds.SETTINGS.value to StretchIcons.Settings,
    ArcActionIds.ADD_SOURCE.value to StretchIcons.Plus,
    ArcActionIds.NEXT_CHANGE.value to StretchIcons.ChevronRight,
    ArcActionIds.PREVIOUS_CHANGE.value to StretchIcons.ChevronLeft,
    ArcActionIds.NEXT_FILE.value to StretchIcons.ArrowDown,
    ArcActionIds.PREVIOUS_FILE.value to StretchIcons.ArrowUp,
    ArcActionIds.MARK_AND_ADVANCE.value to StretchIcons.Check,
    ArcActionIds.TOGGLE_REVIEWED.value to StretchIcons.Check,
    ArcActionIds.SET_ASIDE.value to ArcIcons.SetAside,
    ArcActionIds.FIND_IN_FILE.value to StretchIcons.Search,
    ArcActionIds.OPEN_FILE.value to StretchIcons.File,
    ArcActionIds.TOGGLE_PIN.value to StretchIcons.Pin,
    ArcActionIds.TOGGLE_FULL_FILE.value to ArcIcons.FullFile,
    ArcActionIds.TOGGLE_SPLIT.value to StretchIcons.SplitView,
    ArcActionIds.TOGGLE_WRAP.value to ArcIcons.Wrap,
    ArcActionIds.REMOVE_SOURCE.value to StretchIcons.Close,
    ArcActionIds.OPEN_CHANGE.value to StretchIcons.File,
    ArcActionIds.OPEN_TO_SIDE.value to StretchIcons.SplitView,
    ArcActionIds.OPEN_IN_NEW_WINDOW.value to ArcIcons.TearOut,
    ArcActionIds.OPEN_PINNED.value to StretchIcons.Pin,
    ArcActionIds.OPEN_FULL_FILE.value to ArcIcons.FullFile,
    ArcActionIds.RESTORE.value to StretchIcons.Restore,
)

/** The icon for an action's icon key: a drawer action shows its drawer's icon. */
fun actionIcon(key: String): ImageVector? = icons[key] ?: ArcActionIds.drawerOf(key)?.let(ArcDrawerSlots::byId)?.icon
