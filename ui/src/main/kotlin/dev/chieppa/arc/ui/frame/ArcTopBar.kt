package dev.chieppa.arc.ui.frame

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.ui.menus.ArcBarMenus
import dev.chieppa.arc.ui.settings.SettingsPopover
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.TopBarActions
import dev.chieppa.stretch.frame.WindowTopBar
import org.jetbrains.compose.resources.stringResource

/** The window's top bar: the app's name, its menus, the palette and settings. No review is open, so no name shows. */
@Composable
fun ArcTopBar(workbench: Workbench) {
    WindowTopBar(
        appName = stringResource(Res.string.app_name),
        document = "",
        menus = { ArcBarMenus(workbench.registry, workbench.keymap) },
        actions = {
            TopBarActions(
                onUndo = null,
                onRedo = null,
                onCommands = { workbench.paletteOpen = true },
                onSettings = { workbench.settingsOpen = true },
                settingsOpen = workbench.settingsOpen,
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
