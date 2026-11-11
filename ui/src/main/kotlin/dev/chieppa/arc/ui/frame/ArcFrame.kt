package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcTheme
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.actions.actionIcon
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.conversation.ConversationEffects
import dev.chieppa.arc.ui.conversation.OpenStagedWhenFilled
import dev.chieppa.arc.ui.drawers.arcDrawerSpecs
import dev.chieppa.arc.ui.plugins.PluginDrawers
import dev.chieppa.arc.ui.plugins.PluginEvents
import dev.chieppa.arc.ui.plugins.PluginIconNames
import dev.chieppa.arc.ui.switcher.Switcher
import dev.chieppa.arc.ui.runs.RunOverlays
import dev.chieppa.arc.ui.sources.SourceStrip
import dev.chieppa.arc.ui.status.ArcStatusBar
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.CommandPalette
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.StretchApp
import dev.chieppa.stretch.frame.actionKeys

/**
 * The main window's content: the top bar, the source strip under it across the window, Arc's drawers on both rails, the pane grid in
 * the viewport, the status bar, and the command palette and the switcher over them, all on [workbench]'s state. Every source
 * not read yet is read when it opens, and each repo's GitHub origin is looked up and its runs followed. The run dialogs
 * go over all of it.
 */
@Composable
fun ArcFrame(workbench: Workbench, modifier: Modifier = Modifier) {
    LaunchedEffect(workbench) { workbench.sources.loadAll() }
    LaunchedEffect(workbench, workbench.state.repos) {
        workbench.gitHubRepos = workbench.state.repos.keys.mapNotNull { repo -> workbench.data.gitHubRepo(repo)?.let { repo to it } }.toMap()
    }
    LaunchedEffect(workbench, workbench.gitHubRepos) { workbench.runs.watch.followed = workbench.gitHubRepos.values.toSet() }
    OpenStagedWhenFilled(workbench)
    ConversationEffects(workbench)
    PluginEvents(workbench)
    ArcTheme {
        CompositionLocalProvider(LocalActionText provides actionText(workbench.plugins.actions::label)) {
            StretchApp(
                layout = workbench.frameLayout,
                onLayoutChange = { workbench.frameLayout = it },
                drawers = arcDrawerSpecs(workbench) + PluginDrawers.specs(workbench),
                onResetLayout = workbench::resetLayout,
                modifier = modifier.actionKeys(workbench.keymap, null),
                statusBar = { ArcStatusBar(workbench) },
                placeNewDrawer = PluginDrawers::place,
                topStrip = { SourceStrip(workbench) },
                topBar = { ArcTopBar(workbench) },
            ) {
                ArcViewport(workbench)
            }
            CommandPalette(
                visible = workbench.paletteOpen,
                actions = workbench.registry.available(null).filter { it.id != ArcActionIds.COMMANDS },
                keymap = workbench.keymap,
                onDismiss = { workbench.paletteOpen = false },
                icons = { actionIcon(it) ?: PluginIconNames.of(workbench.plugins.actions.icon(it)) },
            )
            Switcher(workbench)
            RunOverlays(workbench.runs)
        }
    }
}
