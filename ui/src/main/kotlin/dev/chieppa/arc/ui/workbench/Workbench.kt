package dev.chieppa.arc.ui.workbench

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.pane.Pane
import dev.chieppa.arc.ui.actions.registerArcActions
import dev.chieppa.arc.ui.drawers.defaultFrameLayout
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.stretch.core.ActionRegistry
import dev.chieppa.stretch.core.Keymap
import dev.chieppa.stretch.frame.FrameLayout
import dev.chieppa.stretch.frame.PaneLayout

/**
 * The main window's state: the look the person chose, where the drawers are, the panes in the viewport and which
 * overlays are open, with the actions that change them. Views read it and recompose when it changes. [onQuit]
 * closes the app.
 */
class Workbench(preferences: UiPreferences = UiPreferences(), onQuit: () -> Unit = {}) {
    var preferences by mutableStateOf(preferences)
    var frameLayout by mutableStateOf(defaultFrameLayout())
    var paneLayout by mutableStateOf(PaneLayout())

    /** The panes open in the viewport, by the tab id the pane grid knows them by. */
    var panes by mutableStateOf<Map<String, Pane>>(emptyMap())
    var paletteOpen by mutableStateOf(false)
    var settingsOpen by mutableStateOf(false)

    val registry = ActionRegistry()
    val keymap = Keymap(registry)

    init {
        registerArcActions(registry, this, onQuit)
    }

    fun toggleDrawer(id: String) {
        frameLayout = frameLayout.toggle(id)
    }

    fun resetLayout() {
        frameLayout = defaultFrameLayout()
    }
}
