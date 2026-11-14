package dev.chieppa.arc.git.process

import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.core.activity.elapsedMillis
import dev.chieppa.arc.core.activity.failureText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.CancellationException

/**
 * Runs one git command in a repo and collects what it prints, off the calling thread. Cancelling the caller ends the
 * process. Every command gets [GitSettings.ENVIRONMENT] and [GitSettings.CONFIG], and is reported to [activity]: its
 * command line, folder, time and outcome, with git's standard error when it fails. A command that reaches a remote
 * signs in to github.com with the token [credentials] gives, when it gives one ([GitAuth]).
 */
class GitRunner(
    private val program: String = GitSettings.PROGRAM,
    private val activity: ActivityReporter = ActivityReporter.NONE,
    private val credentials: GitCredentials = GitCredentials.NONE,
) {
    /**
     * Runs `git [args]` in [dir], writing [input] to its standard input. Throws [GitException] when the exit code is
     * not in [accept].
     */
    suspend fun run(dir: Path, args: List<String>, input: ByteArray? = null, accept: Set<Int> = setOf(0)): GitResult {
        val started = Instant.now()
        val start = System.nanoTime()
        fun report(outcome: ActivityOutcome, code: String? = null, error: String? = null) =
            activity.report(ActivityEntry(ActivityKind.Git, title(args), outcome, started, elapsedMillis(start), dir.toString(), code, error))
        return try {
            execute(dir, args, input).also { result ->
                if (result.exitCode !in accept) throw GitException(args, result.exitCode, result.stderr)
                report(ActivityOutcome.Succeeded)
            }
        } catch (e: GitException) {
            report(ActivityOutcome.Failed, EXIT + e.exitCode, e.stderr.trim().ifEmpty { EXIT + e.exitCode })
            throw e
        } catch (e: CancellationException) {
            report(ActivityOutcome.Cancelled)
            throw e
        } catch (e: IOException) {
            report(ActivityOutcome.Failed, error = failureText(e))
            throw e
        }
    }

    private suspend fun execute(dir: Path, args: List<String>, input: ByteArray?): GitResult = withContext(Dispatchers.IO) {
        val process = launch(dir, args, discardErrors = false)
        try {
            coroutineScope {
                val out = async(Dispatchers.IO) { process.inputStream.readAllBytes() }
                val err = async(Dispatchers.IO) { process.errorStream.readAllBytes().toString(Charsets.UTF_8) }
                process.outputStream.use { stream -> input?.let(stream::write) }
                val code = runInterruptible { process.waitFor() }
                GitResult(code, out.await(), err.await())
            }
        } finally {
            if (process.isAlive) process.destroyForcibly()
        }
    }

    /**
     * Starts `git [args]` in [dir] and leaves it running, for a process that answers many requests, reported once as
     * started. With [discardErrors] what it writes to standard error is dropped.
     */
    fun start(dir: Path, args: List<String>, discardErrors: Boolean = false): Process {
        val started = Instant.now()
        return try {
            launch(dir, args, discardErrors).also {
                activity.report(ActivityEntry(ActivityKind.Git, title(args), ActivityOutcome.Started, started, place = dir.toString()))
            }
        } catch (e: IOException) {
            activity.report(ActivityEntry(ActivityKind.Git, title(args), ActivityOutcome.Failed, started, 0, dir.toString(), error = failureText(e)))
            throw e
        }
    }

    private fun launch(dir: Path, args: List<String>, discardErrors: Boolean): Process {
        val token = if (GitAuth.reachesRemote(args)) credentials.gitHubToken() else null
        val auth = if (token != null) GitAuth.CONFIG else emptyList()
        val builder = ProcessBuilder(listOf(program) + GitSettings.CONFIG + auth + args).directory(dir.toFile())
        if (discardErrors) builder.redirectError(ProcessBuilder.Redirect.DISCARD)
        builder.environment().putAll(GitSettings.ENVIRONMENT)
        if (token != null) builder.environment().putAll(GitAuth.environment(token))
        return builder.start()
    }

    private fun title(args: List<String>): String = (listOf(GitSettings.PROGRAM) + args).joinToString(" ")

    private companion object {
        const val EXIT = "exit "
    }
}
