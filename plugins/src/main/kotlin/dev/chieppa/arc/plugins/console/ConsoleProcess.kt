package dev.chieppa.arc.plugins.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A process Arc runs for a plugin, by [id], with the [command] it runs: what it has printed so far, as it prints it
 * (a prompt with no line break shows at once, and later output extends that line), its last [maxLines] lines kept,
 * its exit code once it ends, and its input. Every printed piece and the end also go to [onChunk] and [onExit].
 */
class ConsoleProcess(
    val id: String,
    val plugin: String,
    val command: List<String>,
    private val connection: ConsoleConnection,
    scope: CoroutineScope,
    private val maxLines: Int = MAX_LINES,
    onChunk: (ConsoleChunk) -> Unit = {},
    onExit: (Int) -> Unit = {},
) {
    private val printed = MutableStateFlow(ConsolePrinted())
    private val ended = MutableStateFlow<Int?>(null)

    /** What it has printed: the pieces of its last lines, oldest first, and how many pieces it has printed in all. */
    val output: StateFlow<ConsolePrinted> get() = printed

    /** The exit code once it has ended. */
    val exitCode: StateFlow<Int?> get() = ended

    init {
        scope.launch { for (chunk in connection.output) add(chunk, onChunk) }
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
        val pieces = ConsoleLines.split(chunk)
        if (pieces.isEmpty()) return
        val before = printed.value
        printed.value = ConsolePrinted(ConsoleLines.last(before.chunks + pieces, maxLines), before.total + pieces.size)
        onChunk(chunk)
    }

    private companion object {
        const val MAX_LINES = 10_000
    }
}
