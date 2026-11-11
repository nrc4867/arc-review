package dev.chieppa.arc.plugins.host

import dev.chieppa.arc.plugins.protocol.WorkerLimits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Path

/**
 * The one Node process every enabled plugin runs in, each plugin in a worker thread of its own within [limits]. The
 * process starts with the first worker, through [launcher] on the command [command] makes for the folders it may read,
 * and again whenever a worker needs a folder it cannot read yet, its workers then starting again at once. A process
 * that ends, or stops answering its pings, ends every worker in it; their supervisors start them again, and with the
 * first of them the process. What the process itself prints goes to [log].
 */
class NodeHost(
    private val scope: CoroutineScope,
    private val launcher: ProcessLauncher,
    private val command: suspend (Set<Path>) -> LaunchSpec,
    private val timings: HostTimings = HostTimings(),
    private val limits: WorkerLimits = WorkerLimits(),
    val log: PluginLog = PluginLog(),
) : WorkerLauncher {
    private val lock = Mutex()
    @Volatile
    private var process: HostProcess? = null

    override suspend fun launch(spec: WorkerSpec): LineConnection = lock.withLock {
        val root = spec.readRoot.toAbsolutePath().normalize()
        val running = process?.takeIf { it.alive && root in it.grants } ?: start(root)
        running.startWorker(spec.plugin, limits)
    }

    /** Ends the process and every worker in it, at once. */
    fun close() {
        process?.end(WorkerExits.STOPPED)
    }

    private suspend fun start(root: Path): HostProcess {
        val previous = process
        val grants = previous?.grants.orEmpty() + setOf(root)
        if (previous != null && previous.alive) previous.end(WorkerExits.HOST_RESTARTED)
        val connection = launcher.launch(command(grants))
        return HostProcess(connection, grants, scope, timings, log).also { process = it }
    }
}
