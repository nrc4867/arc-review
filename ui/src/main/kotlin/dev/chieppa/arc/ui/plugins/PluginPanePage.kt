package dev.chieppa.arc.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.pane.Page
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** A plugin's pane in the grid: the view of its pane kind, for the source it was opened for, with its props. */
@Composable
fun PluginPanePage(page: Page.Plugin, workbench: Workbench, modifier: Modifier = Modifier) {
    val props = remember(page.props) { runCatching { Json.parseToJsonElement(page.props) as JsonObject }.getOrElse { JsonObject(emptyMap()) } }
    val context = PluginContexts.of(workbench, page.source)
    PluginViewHost(workbench, page.plugin, ViewKind.Pane, page.pane, context, modifier.fillMaxSize().background(Stretch.colors.surfaceCanvas), props)
}
