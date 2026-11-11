package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.ScrollColumn

/** A `Scroll` element: its children one under another, scrolling with Stretch's scrollbar when they do not fit. */
@Composable
fun PluginScroll(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    ScrollColumn(modifier.padding(PluginLayout.padding(node))) { PluginChildren(node.children, scope) }
}
