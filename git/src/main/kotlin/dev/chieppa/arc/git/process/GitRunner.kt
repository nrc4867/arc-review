package dev.chieppa.arc.git.process

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.nio.file.Path

/**
 * Runs one git command in a repo and collects what it prints, off the calling thread. Cancelling the caller ends the
 * process. Every command gets [GitSettings.ENVIRONMENT] and [GitSettings.CONFIG].
 */
class GitRunner(private val program: String = GitSettings.PROGRAM) {
    /**
     * Runs `git [args]` in [dir], writing [input] to its standard input. Throws [GitException] when the exit code is
     * not in [accept].
     */
    suspend fun run(dir: Path, args: List<String>, input: ByteArray? = null, accept: Set<Int> = setOf(0)): GitResult =
        withContext(Dispatchers.IO) {
            val process = start(dir, args)
            try {
                coroutineScope {
                    val out = async(Dispatchers.IO) { process.inputStream.readAllBytes() }
                    val err = async(Dispatchers.IO) { process.errorStream.readAllBytes().toString(Charsets.UTF_8) }
                    process.outputStream.use { stream -> input?.let(stream::write) }
                    val code = runInterruptible { process.waitFor() }
                    val result = GitResult(code, out.await(), err.await())
                    if (code !in accept) throw GitException(args, code, result.stderr)
                    result
                }
            } finally {
                if (process.isAlive) process.destroyForcibly()
            }
        }

    /**
     * Starts `git [args]` in [dir] and leaves it running, for a process that answers many requests. With
     * [discardErrors] what it writes to standard error is dropped.
     */
    fun start(dir: Path, args: List<String>, discardErrors: Boolean = false): Process {
        val builder = ProcessBuilder(listOf(program) + GitSettings.CONFIG + args).directory(dir.toFile())
        if (discardErrors) builder.redirectError(ProcessBuilder.Redirect.DISCARD)
        builder.environment().putAll(GitSettings.ENVIRONMENT)
        return builder.start()
    }
}
