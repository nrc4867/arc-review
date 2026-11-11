package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.LaunchSpec
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/** A console program played by a test: it prints what [print] gives, keeps what is typed in [typed], and [end]s. */
class FakeConsole : ConsoleLauncher, ConsoleConnection {
    private val out = Channel<ConsoleChunk>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()
    val typed = mutableListOf<String>()
    val specs = mutableListOf<LaunchSpec>()

    override val output: ReceiveChannel<ConsoleChunk> get() = out

    override fun launch(spec: LaunchSpec): ConsoleConnection = also { specs += spec }

    fun print(text: String, error: Boolean = false) {
        out.trySend(ConsoleChunk(text, error))
    }

    fun end(code: Int) {
        exit.complete(code)
        out.close()
    }

    override suspend fun send(line: String) {
        typed += line
    }

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() = end(KILLED)

    companion object {
        const val KILLED = 137
    }
}
