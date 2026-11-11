package dev.chieppa.arc.ui.plugins.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.TextField
import kotlinx.serialization.json.JsonPrimitive

/**
 * A `Field` element as Stretch's text field. Typing shows at once and calls `onChange`; the plugin's `value` replaces
 * the text when it differs. Enter calls `onSubmit`.
 */
@Composable
fun PluginField(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val value = Props.string(node.props, PropNames.VALUE).orEmpty()
    var text by remember(node.id) { mutableStateOf(value) }
    LaunchedEffect(value) { text = value }
    val submits = Props.handler(node.props, PropNames.ON_SUBMIT)
    TextField(
        text,
        {
            text = it
            scope.call(node.id, PropNames.ON_CHANGE, JsonPrimitive(it))
        },
        modifier.onPreviewKeyEvent { event ->
            val enter = submits && event.type == KeyEventType.KeyDown && event.key == Key.Enter
            if (enter) scope.call(node.id, PropNames.ON_SUBMIT, JsonPrimitive(text))
            enter
        },
        placeholder = Props.string(node.props, PropNames.PLACEHOLDER).orEmpty(),
        mono = Props.boolean(node.props, PropNames.MONO) == true,
    )
}
