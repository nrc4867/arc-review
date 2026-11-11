package dev.chieppa.arc.ui.plugins

import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.PluginConnection
import dev.chieppa.arc.plugins.host.PluginLauncher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

/** A console process played by a test: it prints what [print] gives and keeps what is typed into it in [typed]. */
class FakeConsole : PluginLauncher, PluginConnection {
    private val out = Channel<String>(Channel.UNLIMITED)
    private val err = Channel<String>(Channel.UNLIMITED)
    private val exit = CompletableDeferred<Int>()
    val typed = mutableListOf<String>()

    override val lines: ReceiveChannel<String> get() = out
    override val logs: ReceiveChannel<String> get() = err

    override fun launch(spec: LaunchSpec): PluginConnection = this

    fun print(line: String) {
        out.trySend(line)
    }

    override suspend fun send(line: String) {
        typed += line
    }

    override suspend fun awaitExit(): Int = exit.await()

    override fun kill() {
        exit.complete(0)
    }
}
