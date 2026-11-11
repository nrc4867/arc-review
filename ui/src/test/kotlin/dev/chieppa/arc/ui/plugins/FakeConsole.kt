package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.console.ConsoleChunk
import dev.chieppa.arc.plugins.console.ConsoleConnection
import dev.chieppa.arc.plugins.console.ConsoleLauncher
import dev.chieppa.arc.plugins.host.LaunchSpec
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/** A console process played by a test: it prints what [print] gives and keeps what is typed into it in [typed]. */
class FakeConsole : ConsoleLauncher, ConsoleConnection {
    private val out = Channel<ConsoleChunk>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()
    val typed = mutableListOf<String>()

    override val output: ReceiveChannel<ConsoleChunk> get() = out

    override fun launch(spec: LaunchSpec): ConsoleConnection = this

    fun print(text: String) {
        out.trySend(ConsoleChunk(text))
    }

    override suspend fun send(line: String) {
        typed += line
    }

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() {
        exit.complete(0)
    }
}
