package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Something running that Arc talks to in lines: a process, or one plugin's worker in the shared Node process. Its
 * [lines] carry the protocol, its [logs] what it printed; [send] gives it a line, and it ends with an exit code.
 */
interface LineConnection {
    val lines: ReceiveChannel<String>
    val logs: ReceiveChannel<String>

    /** Sends [line] to it. */
    suspend fun send(line: String)

    /** Waits for it to end and gives its exit code. */
    suspend fun awaitExit(): Int

    /** Ends it and everything it started, at once. */
    fun kill()
}
