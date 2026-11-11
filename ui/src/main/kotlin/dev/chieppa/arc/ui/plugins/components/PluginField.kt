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
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugin_secret_not_kept
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.ui.TextField
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.stringResource

/**
 * A `Field` element as Stretch's text field. Typing shows at once and calls `onChange`; the plugin's `value` replaces
 * the text when it differs. Enter calls `onSubmit`. A `secret` field is masked, and highlighted where secrets do not
 * outlast this run of Arc.
 */
@Composable
fun PluginField(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val value = Props.string(node.props, PropNames.VALUE).orEmpty()
    var text by remember(node.id) { mutableStateOf(value) }
    LaunchedEffect(value) { text = value }
    val submits = Props.handler(node.props, PropNames.ON_SUBMIT)
    val secret = Props.boolean(node.props, PropNames.SECRET) == true
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
        error = if (secret && !scope.secretsLast) stringResource(Res.string.plugin_secret_not_kept) else null,
        mono = Props.boolean(node.props, PropNames.MONO) == true,
        masked = secret,
    )
}
