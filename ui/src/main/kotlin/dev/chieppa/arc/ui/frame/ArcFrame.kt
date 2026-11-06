package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcTheme
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.conversation.OpenStagedWhenFilled
import dev.chieppa.arc.ui.drawers.arcDrawerSpecs
import dev.chieppa.arc.ui.finder.FileFinder
import dev.chieppa.arc.ui.sources.SourceStrip
import dev.chieppa.arc.ui.status.ArcStatusBar
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.CommandPalette
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.StretchApp
import dev.chieppa.stretch.frame.actionKeys

/**
 * The main window's content: the top bar, the source strip under it across the window, Arc's drawers on both rails, the pane grid in
 * the viewport, the status bar, and the palette and file finder over them, all on [workbench]'s state. Every source
 * not read yet is read when it opens.
 */
@Composable
fun ArcFrame(workbench: Workbench, modifier: Modifier = Modifier) {
    LaunchedEffect(workbench) { workbench.sources.loadAll() }
    OpenStagedWhenFilled(workbench)
    ArcTheme {
        CompositionLocalProvider(LocalActionText provides actionText()) {
            StretchApp(
                layout = workbench.frameLayout,
                onLayoutChange = { workbench.frameLayout = it },
                drawers = arcDrawerSpecs(workbench),
                onResetLayout = workbench::resetLayout,
                modifier = modifier.actionKeys(workbench.keymap, null),
                statusBar = { ArcStatusBar(workbench) },
                topStrip = { SourceStrip(workbench) },
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
            FileFinder(workbench)
        }
    }
}
