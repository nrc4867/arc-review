package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.PluginConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A process Arc runs for a plugin, by [id], with the [command] it runs: what it has printed so far, line by line, its
 * exit code once it ends, and its input. Every printed line and the end also go to [onChunk] and [onExit].
 */
class ConsoleProcess(
    val id: String,
    val plugin: String,
    val command: List<String>,
    private val connection: PluginConnection,
    scope: CoroutineScope,
    private val maxLines: Int = MAX_LINES,
    onChunk: (ConsoleChunk) -> Unit = {},
    onExit: (Int) -> Unit = {},
) {
    private val printed = MutableStateFlow<List<ConsoleChunk>>(emptyList())
    private val ended = MutableStateFlow<Int?>(null)

    /** Everything printed so far, oldest first, at most the last lines kept. */
    val output: StateFlow<List<ConsoleChunk>> get() = printed

    /** The exit code once it has ended. */
    val exitCode: StateFlow<Int?> get() = ended

    init {
        scope.launch { for (line in connection.lines) add(ConsoleChunk(line + LINE_BREAK), onChunk) }
        scope.launch { for (line in connection.logs) add(ConsoleChunk(line + LINE_BREAK, error = true), onChunk) }
        scope.launch {
            val code = connection.awaitExit()
            ended.value = code
            onExit(code)
        }
    }

    /** Types [text] into its input, as a line. */
    suspend fun write(text: String) {
        if (ended.value == null) connection.send(text)
    }

    fun kill() {
        connection.kill()
    }

    @Synchronized
    private fun add(chunk: ConsoleChunk, onChunk: (ConsoleChunk) -> Unit) {
        printed.value = (printed.value + chunk).takeLast(maxLines)
        onChunk(chunk)
    }

    private companion object {
        const val MAX_LINES = 10_000
        const val LINE_BREAK = "\n"
    }
}
