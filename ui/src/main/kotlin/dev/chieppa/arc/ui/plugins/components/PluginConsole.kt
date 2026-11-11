package dev.chieppa.arc.ui.plugins.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.plugin_console_exited
import dev.chieppa.arc.design.generated.plugin_console_input
import dev.chieppa.arc.plugins.api.PropNames
import dev.chieppa.arc.plugins.console.ConsoleProcess
import dev.chieppa.arc.plugins.view.Props
import dev.chieppa.arc.plugins.view.ViewNode
import dev.chieppa.arc.ui.plugins.PluginScope
import dev.chieppa.stretch.console.Console
import dev.chieppa.stretch.console.ConsoleStream
import dev.chieppa.stretch.console.rememberConsoleState
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.TextField
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * A `Console` element: the output of the process its `handle` names in Stretch's console, everything printed so far
 * and then as it comes, over a field whose lines go to the process's input when Enter is pressed. Once the process has
 * ended the field shows its exit code and takes no input.
 */
@Composable
fun PluginConsole(node: ViewNode, scope: PluginScope, modifier: Modifier = Modifier) {
    val process = Props.string(node.props, PropNames.HANDLE)?.let(scope::console) ?: return
    val height = Props.number(node.props, PropNames.HEIGHT)
    Column(if (height != null) modifier.fillMaxWidth().height(height.toFloat().dp) else modifier) {
        ProcessOutput(process, Modifier.weight(1f).fillMaxWidth())
        Divider()
        ProcessInput(process)
    }
}

@Composable
private fun ProcessOutput(process: ConsoleProcess, modifier: Modifier) {
    val state = rememberConsoleState()
    LaunchedEffect(process) {
        var shown = 0L
        process.output.collect { printed ->
            val fresh = (printed.total - shown).coerceAtMost(printed.chunks.size.toLong()).toInt()
            printed.chunks.takeLast(fresh).forEach { state.append(it.text, if (it.error) ConsoleStream.Err else ConsoleStream.Out) }
            shown = printed.total
        }
    }
    Console(state, modifier)
}

@Composable
private fun ProcessInput(process: ConsoleProcess) {
    val code by process.exitCode.collectAsState()
    var line by remember(process) { mutableStateOf("") }
    val coroutines = rememberCoroutineScope()
    val ended = code?.let { stringResource(Res.string.plugin_console_exited, it) }
    TextField(
        if (ended != null) "" else line,
        { if (ended == null) line = it },
        Modifier.fillMaxWidth().padding(StretchSpacing.space4).onPreviewKeyEvent { event ->
            val enter = event.type == KeyEventType.KeyDown && event.key == Key.Enter && ended == null
            if (enter) {
                val typed = line
                line = ""
                coroutines.launch { process.write(typed) }
            }
            enter
        },
        placeholder = ended ?: stringResource(Res.string.plugin_console_input),
        mono = true,
    )
}
