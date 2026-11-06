package dev.chieppa.arc.ui.frame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.ui.menus.ArcBarMenus
import dev.chieppa.arc.ui.settings.SettingsPopover
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.TopBarActions
import dev.chieppa.stretch.frame.WindowTopBar
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** The window's top bar: the app's name, its menus, the review selector and "Go to anything", the palette and settings. */
@Composable
fun ArcTopBar(workbench: Workbench) {
    WindowTopBar(
        appName = stringResource(Res.string.app_name),
        document = "",
        menus = { ArcBarMenus(workbench.registry, workbench.keymap) },
        modes = {
            Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space24), verticalAlignment = Alignment.CenterVertically) {
                ReviewSelector(workbench)
                GoToAnythingField({ workbench.paletteOpen = true })
            }
        },
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
