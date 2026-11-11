package dev.chieppa.arc.plugins.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

/** Runs programs as processes, off the calling thread; cancelling the caller ends the process. */
object ProcessCommandRunner : CommandRunner {
    override suspend fun run(command: List<String>, directory: java.nio.file.Path): CommandResult = withContext(Dispatchers.IO) {
        val process = ProcessBuilder(command).directory(directory.toFile()).start()
        try {
            process.outputStream.close()
            coroutineScope {
                val out = async(Dispatchers.IO) { process.inputStream.readAllBytes().toString(Charsets.UTF_8) }
                val err = async(Dispatchers.IO) { process.errorStream.readAllBytes().toString(Charsets.UTF_8) }
                val code = runInterruptible { process.waitFor() }
                CommandResult(code, out.await(), err.await())
            }
        } finally {
            if (process.isAlive) process.destroyForcibly()
        }
    }
}
