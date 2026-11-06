package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.menus.ArcBarMenus
import dev.chieppa.arc.ui.settings.SettingsPopover
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.TopBarActions
import dev.chieppa.stretch.frame.WindowTopBar
import dev.chieppa.stretch.frame.capsFor
import org.jetbrains.compose.resources.stringResource

/**
 * The window's top bar: the app's name and menus, the review selector after them, "Go to anything" centred on the
 * whole bar, and the palette and settings at the end, each showing the palette's own key.
 */
@Composable
fun ArcTopBar(workbench: Workbench) {
    val paletteKeys = workbench.registry[ArcActionIds.GO_TO_ANYTHING]?.let(workbench.keymap::capsFor)
    WindowTopBar(
        appName = stringResource(Res.string.app_name),
        document = "",
        menus = { ArcBarMenus(workbench.registry, workbench.keymap) },
        leading = { ReviewSelector(workbench) },
        center = { GoToAnythingField(paletteKeys, { workbench.paletteOpen = true }) },
        actions = {
            TopBarActions(
                onUndo = null,
                onRedo = null,
                onCommands = { workbench.paletteOpen = true },
                onSettings = { workbench.settingsOpen = true },
                settingsOpen = workbench.settingsOpen,
                commandsKeys = paletteKeys,
                settingsPopup = {
                    SettingsPopover(
                        open = workbench.settingsOpen,
                        preferences = workbench.preferences,
                        onChange = { workbench.preferences = it },
                        onDismiss = { workbench.settingsOpen = false },
                    )
                },
            )
        },
    )
}
