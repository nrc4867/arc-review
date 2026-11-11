package dev.chieppa.arc.plugins.host

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Writer

/** A [PluginConnection] to a real [process], its streams read and written on [scope]'s IO threads as UTF-8 lines. */
class ProcessConnection(private val process: Process, scope: CoroutineScope) : PluginConnection {
    private val out = Channel<String>(Channel.UNLIMITED)
    private val err = Channel<String>(Channel.UNLIMITED)
    private val input: Writer = process.outputStream.writer(Charsets.UTF_8)

    override val lines: ReceiveChannel<String> get() = out
    override val logs: ReceiveChannel<String> get() = err

    init {
        scope.launch(Dispatchers.IO) { pump(process.inputStream, out) }
        scope.launch(Dispatchers.IO) { pump(process.errorStream, err) }
    }

    override suspend fun send(line: String) {
        withContext(Dispatchers.IO) {
            synchronized(input) {
                input.write(line)
                input.write(LINE_BREAK)
                input.flush()
            }
        }
    }

    override suspend fun awaitExit(): Int = runInterruptible(Dispatchers.IO) { process.waitFor() }

    override fun kill() {
        process.descendants().forEach { it.destroyForcibly() }
        process.destroyForcibly()
    }

    private fun pump(stream: InputStream, into: Channel<String>) {
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            while (true) {
                val line = runCatching { reader.readLine() }.getOrNull() ?: break
                into.trySend(line)
            }
        }
        into.close()
    }

    private companion object {
        const val LINE_BREAK = "\n"
    }
}
