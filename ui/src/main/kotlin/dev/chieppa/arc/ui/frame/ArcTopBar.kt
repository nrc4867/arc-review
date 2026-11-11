package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.design.generated.notifications
import dev.chieppa.stretch.frame.BarAction
import dev.chieppa.stretch.ui.badgeCount
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.menus.ArcBarMenus
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.TopBarActions
import dev.chieppa.stretch.frame.WindowTopBar
import dev.chieppa.stretch.frame.capsFor
import org.jetbrains.compose.resources.stringResource

/**
 * The window's top bar: the app's name and menus, "Go to anything" centred on the whole bar, which opens the switcher, and at the end the notifications bell, its badge counting notifications not
 * seen yet, which opens the Pull requests drawer, then the command palette, showing its own key, and settings, which opens the Settings page.
 */
@Composable
fun ArcTopBar(workbench: Workbench) {
    val switcherKeys = workbench.registry[ArcActionIds.GO_TO_ANYTHING]?.let(workbench.keymap::capsFor)
    val commandsKeys = workbench.registry[ArcActionIds.COMMANDS]?.let(workbench.keymap::capsFor)
    WindowTopBar(
        appName = stringResource(Res.string.app_name),
        document = "",
        menus = { ArcBarMenus(workbench.registry, workbench.keymap) },
        center = { GoToAnythingField(switcherKeys, { workbench.switcher.show() }) },
        actions = {
            TopBarActions(
                onUndo = null,
                onRedo = null,
                onCommands = { workbench.paletteOpen = true },
                onSettings = workbench.panes::openSettings,
                leading = listOf(BarAction(ArcIcons.Bell, stringResource(Res.string.notifications), workbench::openPullRequests, badge = badgeCount(workbench.watch.unseen))),
                commandsKeys = commandsKeys,
            )
        },
    )
}
