package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * One party inside the shared Node process as a [LineConnection]: a plugin's worker, or the process's main thread.
 * [HostProcess] hands it the lines and log lines meant for it; what it sends goes out through [outbound], and
 * [onKill] ends it in the process.
 */
internal class RoutedConnection(
    private val outbound: suspend (String) -> Unit,
    private val onKill: () -> Unit = {},
) : LineConnection {
    private val inbound = Channel<String>(Channel.UNLIMITED)
    private val printed = Channel<String>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()

    override val lines: ReceiveChannel<String> get() = inbound
    override val logs: ReceiveChannel<String> get() = printed

    val ended: Boolean get() = exit.isCompleted

    override suspend fun send(line: String) = outbound(line)

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() {
        if (ended) return
        onKill()
        end(WorkerExits.STOPPED)
    }

    fun deliver(line: String) {
        inbound.trySend(line)
    }

    fun log(line: String) {
        printed.trySend(line)
    }

    /** Ends it with [code]; the first end counts. */
    fun end(code: Int) {
        if (!exit.complete(code)) return
        inbound.close()
        printed.close()
    }
}
