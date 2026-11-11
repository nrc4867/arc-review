package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope

/** A `Column` element: its children one under another with its gap and padding. */
@Composable
fun PluginColumn(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    Column(modifier.padding(PluginLayout.padding(node)), verticalArrangement = Arrangement.spacedBy(PluginLayout.gap(node))) {
        PluginChildren(node.children, scope)
    }
}
