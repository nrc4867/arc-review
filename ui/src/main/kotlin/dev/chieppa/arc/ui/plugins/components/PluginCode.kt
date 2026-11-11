package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.stretch.code.CodeLanguages
import dev.chieppa.stretch.code.CodeText
import dev.chieppa.stretch.code.CodeViewer

/** A `Code` element as Stretch's code viewer, coloured for the language it names. */
@Composable
fun PluginCode(node: ViewNode, modifier: Modifier = Modifier) {
    val text = Props.string(node.props, PropNames.TEXT).orEmpty()
    val code = remember(text) { CodeText.of(text) }
    CodeViewer(code, modifier, language = CodeLanguages.named(Props.string(node.props, PropNames.LANGUAGE)), ruler = false)
}
