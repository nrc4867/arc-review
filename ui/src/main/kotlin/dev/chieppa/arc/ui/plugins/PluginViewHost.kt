package dev.chieppa.arc.ui.plugins

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.mcp.PngImages
import dev.chieppa.arc.ui.plugins.components.PluginChildren
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import kotlinx.serialization.json.JsonObject

/**
 * Plugin [plugin]'s [contribution] of [kind], shown for [context] with [props]: mounted in the plugin while on screen
 * and drawn from the tree it sends, each element as its Stretch composable. Nothing shows while the plugin is off. A
 * panel or pane is drawn through a layer, on its surface's colour, so Arc's MCP server can picture it alone.
 */
@Composable
fun PluginViewHost(
    workbench: Workbench,
    plugin: String,
    kind: ViewKind,
    contribution: String,
    context: CallContext,
    modifier: Modifier = Modifier,
    props: JsonObject = JsonObject(emptyMap()),
) {
    val manager = workbench.plugins.manager ?: return
    val instance = workbench.plugins.entries.firstOrNull { it.id == plugin }?.instance ?: return
    val view = remember(instance, kind, contribution, context, props) { instance.mount(kind, contribution, context, props) }
    DisposableEffect(view) { onDispose { view.close() } }
    val nodes by view.nodes.collectAsState()
    val scope = remember(view) { PluginScope(view, manager.secretsLast) { manager.consoles[it] } }
    if (kind == ViewKind.Settings) {
        Column(modifier) { PluginChildren(nodes, scope) }
        return
    }
    val layer = rememberGraphicsLayer()
    val surface = if (kind == ViewKind.Panel) Stretch.colors.surfacePanel else Stretch.colors.surfaceCanvas
    DisposableEffect(view, layer) {
        workbench.mcp.captures.register(view.id) { layer.toImageBitmap().let(PngImages::encode) }
        onDispose { workbench.mcp.captures.remove(view.id) }
    }
    val drawn = Modifier.drawWithContent {
        layer.record {
            drawRect(surface)
            this@drawWithContent.drawContent()
        }
        drawLayer(layer)
    }
    Column(drawn.then(modifier)) { PluginChildren(nodes, scope) }
}
