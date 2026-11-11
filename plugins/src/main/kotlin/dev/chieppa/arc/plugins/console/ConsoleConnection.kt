package dev.chieppa.arc.plugins.console

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * A program Arc runs for a plugin, read as it prints rather than by lines: its [output] carries each piece of text as
 * soon as it is written, from standard output and error alike, so a prompt with no line break shows at once. [send]
 * types a line into its input, and it ends with an exit code.
 */
interface ConsoleConnection {
    val output: ReceiveChannel<ConsoleChunk>

    /** Types [line] into its input, with a line break. */
    suspend fun send(line: String)

    /** Waits for it to end and gives its exit code. */
    suspend fun awaitExit(): Int

    /** Ends it and everything it started, at once. */
    fun kill()
}
