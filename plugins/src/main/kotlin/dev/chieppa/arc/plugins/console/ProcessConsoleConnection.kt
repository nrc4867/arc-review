package dev.chieppa.arc.plugins.console

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Writer

/**
 * A [ConsoleConnection] to a real [process]: both its outputs read on [scope]'s IO threads as UTF-8, each read passed
 * on as it returns, whether or not it ends a line.
 */
class ProcessConsoleConnection(private val process: Process, scope: CoroutineScope) : ConsoleConnection {
    private val chunks = Channel<ConsoleChunk>(Channel.UNLIMITED)
    private val input: Writer = process.outputStream.writer(Charsets.UTF_8)

    override val output: ReceiveChannel<ConsoleChunk> get() = chunks

    init {
        val out = scope.launch(Dispatchers.IO) { pump(process.inputStream, error = false) }
        val err = scope.launch(Dispatchers.IO) { pump(process.errorStream, error = true) }
        scope.launch {
            joinAll(out, err)
            chunks.close()
        }
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

    private fun pump(stream: InputStream, error: Boolean) {
        InputStreamReader(stream, Charsets.UTF_8).use { reader ->
            val buffer = CharArray(BUFFER_CHARS)
            while (true) {
                val read = runCatching { reader.read(buffer) }.getOrDefault(END)
                if (read == END) break
                if (read > 0) chunks.trySend(ConsoleChunk(String(buffer, 0, read), error))
            }
        }
    }

    private companion object {
        const val LINE_BREAK = "\n"
        const val BUFFER_CHARS = 8192
        const val END = -1
    }
}
