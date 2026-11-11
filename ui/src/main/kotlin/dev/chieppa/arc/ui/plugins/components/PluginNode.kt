package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.ComponentNames
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Label

/** One element of a plugin view as its Stretch composable; an element Arc does not know draws nothing. */
@Composable
fun PluginNode(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    when (node.type) {
        ViewNode.TEXT -> Label(node.text.orEmpty(), Stretch.type.body, Stretch.colors.ink, modifier)
        ComponentNames.COLUMN -> PluginColumn(node, scope, modifier)
        ComponentNames.ROW -> PluginRow(node, scope, modifier)
        ComponentNames.SPLIT -> PluginSplit(node, scope, modifier)
        ComponentNames.SCROLL -> PluginScroll(node, scope, modifier)
        ComponentNames.LIST -> PluginList(node, scope, modifier)
        ComponentNames.LIST_ROW -> PluginListRow(node, selected = false, onClick = null, modifier)
        ComponentNames.TABLE -> PluginTable(node, scope, modifier)
        ComponentNames.BUTTON -> PluginButton(node, scope, modifier)
        ComponentNames.TOGGLE -> PluginToggle(node, scope, modifier)
        ComponentNames.FIELD -> PluginField(node, scope, modifier)
        ComponentNames.SELECT -> PluginSelect(node, scope, modifier)
        ComponentNames.TEXT -> PluginText(node, modifier)
        ComponentNames.MARKDOWN -> PluginMarkdown(node, modifier)
        ComponentNames.CODE -> PluginCode(node, modifier)
        ComponentNames.CONSOLE -> PluginConsole(node, scope, modifier)
    }
}
