package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.LineConnection
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
    private val connection: LineConnection,
    scope: CoroutineScope,
    private val maxLines: Int = MAX_LINES,
    onChunk: (ConsoleChunk) -> Unit = {},
    onExit: (Int) -> Unit = {},
) {
    private val printed = MutableStateFlow(ConsolePrinted())
    private val ended = MutableStateFlow<Int?>(null)

    /** What it has printed: the last lines kept, oldest first, and how many it has printed in all. */
    val output: StateFlow<ConsolePrinted> get() = printed

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
        val before = printed.value
        printed.value = ConsolePrinted((before.chunks + chunk).takeLast(maxLines), before.total + 1)
        onChunk(chunk)
    }

    private companion object {
        const val MAX_LINES = 10_000
        const val LINE_BREAK = "\n"
    }
}
