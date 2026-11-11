package dev.chieppa.arc.ui.mcp

import dev.chieppa.arc.mcp.dev.DevWindow
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.plugins.PluginContexts
import dev.chieppa.arc.ui.plugins.PluginIds
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.withContext

/**
 * The window as the plugin development tools reach it, on its own thread: the sources in its strip, opening a plugin's
 * drawer or pane as the person would, and picturing a plugin view from [captures]. Nothing else of the window is reached.
 */
class WindowDevWindow(private val workbench: Workbench, private val captures: ViewCaptures) : DevWindow {
    override suspend fun context(ref: String?): CallContext? = onWindow {
        if (ref == null) return@onWindow PluginContexts.of(workbench, workbench.state.active)
        workbench.state.workspace.sources.firstOrNull { it.ref == ref }?.let { PluginContexts.of(workbench, it) }
    }

    override suspend fun open(plugin: String, kind: ViewKind, contribution: String) = onWindow {
        when (kind) {
            ViewKind.Panel -> {
                val drawer = PluginIds.drawer(plugin, contribution)
                if (workbench.frameLayout.drawers.firstOrNull { it.id == drawer }?.open != true) workbench.toggleDrawer(drawer)
            }
            ViewKind.Pane -> workbench.panes.openPluginPane(plugin, contribution, PANE_PROPS, workbench.state.active)
            ViewKind.Settings -> Unit
        }
    }

    override suspend fun capture(view: String): ByteArray? = onWindow { captures.capture(view) }

    private suspend fun <T> onWindow(block: suspend () -> T): T = withContext(workbench.dispatchers.main) { block() }

    private companion object {
        /** A pane opened by the tools carries no props. */
        const val PANE_PROPS = "{}"
    }
}
