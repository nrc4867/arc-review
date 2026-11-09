package dev.chieppa.arc.ui.runs

import dev.chieppa.arc.core.actions.WorkflowRun
import dev.chieppa.arc.host.github.api.GitHubException
import dev.chieppa.arc.host.github.repo.GitHubRepo
import dev.chieppa.arc.ui.services.ActionsService
import dev.chieppa.stretch.console.ConsoleState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Reading a job's whole log from GitHub into a console as it arrives; nothing of it is written to disk. */
object FullLog {
    /** Replaces what [console] shows with job [job]'s whole log, read on [io]; false when GitHub has no log any more. */
    suspend fun into(console: ConsoleState, service: ActionsService, run: WorkflowRun, job: Long, io: CoroutineDispatcher): Boolean = coroutineScope {
        val repo = GitHubRepo.parse(run.repo) ?: return@coroutineScope false
        console.clear()
        val lines = Channel<String>(Channel.UNLIMITED)
        var found = false
        launch {
            try {
                found = withContext(io) { service.fullLog(repo, job) { lines.trySend(it) } }
            } catch (_: GitHubException) {
            } finally {
                lines.close()
            }
        }
        for (line in lines) console.append(line.substringAfter(STAMP_END, line) + "\n")
        found
    }

    private const val STAMP_END = "Z "
}
