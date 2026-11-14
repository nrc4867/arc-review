package dev.chieppa.arc.ui.activity

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.chieppa.stretch.console.Console
import dev.chieppa.stretch.console.rememberConsoleState
import kotlinx.coroutines.channels.Channel

/**
 * The Console drawer: [console]'s activity log in the channel chosen, oldest at the top and following the newest at the
 * bottom, each entry a line and each failure in the error ink with its reason under it, selectable to copy. Shows every
 * entry or only failures, as [ActivityConsole.filter] says.
 */
@Composable
fun ConsoleDrawer(console: ActivityConsole) {
    val state = rememberConsoleState()
    val labels = activityLabels()
    val lines = remember(labels) { ActivityLines(labels) }
    LaunchedEffect(console, console.filter, console.channel, console.clears, lines) {
        state.clear()
        val channel = console.channel
        val reported = Channel<Unit>(Channel.CONFLATED)
        val listening = console.log.listen { reported.trySend(Unit) }
        try {
            var shown = 0L
            while (true) {
                for (entry in console.log.since(shown, channel)) {
                    shown = entry.id
                    if (console.filter.shows(entry)) lines.chunks(entry, withChannel = channel == null).forEach { state.append(it.text, it.stream) }
                }
                console.refreshChannels()
                reported.receive()
            }
        } finally {
            listening.close()
        }
    }
    Console(state, Modifier.fillMaxSize())
}
