package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcTheme
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.drawers.arcDrawerSpecs
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.CommandPalette
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.StretchApp
import dev.chieppa.stretch.frame.actionKeys

/**
 * The main window's content: the top bar, Arc's drawers on both rails, the pane grid in the viewport and the
 * palette over it, all on [workbench]'s state.
 */
@Composable
fun ArcFrame(workbench: Workbench, modifier: Modifier = Modifier) {
    ArcTheme {
        CompositionLocalProvider(LocalActionText provides actionText()) {
            StretchApp(
                layout = workbench.frameLayout,
                onLayoutChange = { workbench.frameLayout = it },
                drawers = arcDrawerSpecs(),
                onResetLayout = workbench::resetLayout,
                modifier = modifier.actionKeys(workbench.keymap, null),
                topBar = { ArcTopBar(workbench) },
            ) {
                ArcViewport(workbench)
            }
            CommandPalette(
                visible = workbench.paletteOpen,
                actions = workbench.registry.available(null).filter { it.id != ArcActionIds.GO_TO_ANYTHING },
                keymap = workbench.keymap,
                onDismiss = { workbench.paletteOpen = false },
                icons = ::actionIcon,
            )
        }
    }
}
