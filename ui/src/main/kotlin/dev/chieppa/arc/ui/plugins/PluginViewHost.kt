package dev.chieppa.arc.ui.plugins

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.protocol.CallContext
import dev.chieppa.arc.plugins.protocol.ViewKind
import dev.chieppa.arc.ui.plugins.components.PluginChildren
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.serialization.json.JsonObject

/**
 * Plugin [plugin]'s [contribution] of [kind], shown for [context] with [props]: mounted in the plugin while on screen
 * and drawn from the tree it sends, each element as its Stretch composable. Nothing shows while the plugin is off.
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
    val scope = remember(view) { PluginScope(view) { manager.consoles[it] } }
    Column(modifier) { PluginChildren(nodes, scope) }
}
