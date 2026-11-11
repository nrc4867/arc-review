package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.channels.ReceiveChannel

/**
 * A running plugin process as Arc talks to it: the [lines] it writes on its standard output, which carry the
 * protocol, the [logs] it writes on its standard error, a way to [send] it a line, and its end.
 */
interface PluginConnection {
    val lines: ReceiveChannel<String>
    val logs: ReceiveChannel<String>

    /** Writes [line] and a line break to the process's standard input. */
    suspend fun send(line: String)

    /** Waits for the process to end and gives its exit code. */
    suspend fun awaitExit(): Int

    /** Ends the process and everything it started, at once. */
    fun kill()
}
