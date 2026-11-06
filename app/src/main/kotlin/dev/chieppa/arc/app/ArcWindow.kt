package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.window.rememberWindowState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.app_name
import dev.chieppa.arc.ui.actions.actionText
import dev.chieppa.arc.ui.frame.ArcFrame
import dev.chieppa.arc.ui.menus.appMenus
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.LocalActionText
import dev.chieppa.stretch.frame.MacMenuBar
import dev.chieppa.stretch.frame.StretchWindow
import dev.chieppa.stretch.theme.StretchTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Arc's main window, opening with the [initial] look. Each change to the look goes to [onPreferencesChange];
 * [onExit] ends the app.
 */
@Composable
fun ArcWindow(initial: UiPreferences, onPreferencesChange: suspend (UiPreferences) -> Unit, onExit: () -> Unit) {
    val exit by rememberUpdatedState(onExit)
    val workbench = remember { Workbench(initial, onQuit = { exit() }) }
    StretchWindow(
        onCloseRequest = onExit,
        title = stringResource(Res.string.app_name),
        state = rememberWindowState(width = AppConstants.WINDOW_WIDTH, height = AppConstants.WINDOW_HEIGHT),
    ) {
        SavePreferences(workbench, onPreferencesChange)
        CompositionLocalProvider(LocalActionText provides actionText()) {
            MacMenuBar(appMenus(), workbench.registry, workbench.keymap, null, onSettings = { workbench.settingsOpen = true }, onQuit = onExit)
        }
        StretchTheme(workbench.preferences.theme, scale = workbench.preferences.interfaceSize) {
            ArcFrame(workbench)
        }
    }
}
