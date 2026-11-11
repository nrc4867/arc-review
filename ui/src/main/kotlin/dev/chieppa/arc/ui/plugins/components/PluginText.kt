package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Label

/** A `Text` element: its text children in the ink its `tone` names, in the code face when `mono`. */
@Composable
fun PluginText(node: ViewNode, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val tone = Props.string(node.props, PropNames.TONE)
    val color = when (tone) {
        MUTED -> c.inkMuted
        STRONG -> c.inkStrong
        ERROR -> c.errorInk
        else -> c.ink
    }
    val base = if (Props.boolean(node.props, PropNames.MONO) == true) Stretch.type.valueSm else Stretch.type.body
    val style = if (tone == STRONG) base.copy(fontWeight = FontWeight.SemiBold) else base
    Label(node.children.joinToString("") { it.text.orEmpty() }, style, color, modifier)
}

private const val MUTED = "muted"
private const val STRONG = "strong"
private const val ERROR = "error"
