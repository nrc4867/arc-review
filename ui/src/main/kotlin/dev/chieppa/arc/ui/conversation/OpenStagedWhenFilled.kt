package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.ui.drawers.DrawerIds
import dev.chieppa.arc.ui.settings.StagedPlace
import dev.chieppa.arc.ui.workbench.Workbench

/**
 * Opens the Staged comments drawer the first time the workspace has staged comments, so it starts closed while there are
 * none and the person's own closing of it stays. Nothing opens while the staged batch sits on the source's home.
 */
@Composable
fun OpenStagedWhenFilled(workbench: Workbench) {
    val filled = stagedComments(workbench).isNotEmpty()
    val onRail = workbench.preferences.stagedPlace == StagedPlace.Rail
    LaunchedEffect(filled, onRail) {
        if (!filled || !onRail || workbench.stagedShown) return@LaunchedEffect
        workbench.stagedShown = true
        if (workbench.frameLayout[DrawerIds.STAGED_COMMENTS]?.open == false) workbench.toggleDrawer(DrawerIds.STAGED_COMMENTS)
    }
}
