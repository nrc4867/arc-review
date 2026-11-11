package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.stretch.markdown.MarkdownView

/** A `Markdown` element as Stretch's rendered Markdown. */
@Composable
fun PluginMarkdown(node: ViewNode, modifier: Modifier = Modifier) {
    MarkdownView(Props.string(node.props, PropNames.TEXT).orEmpty(), modifier)
}
