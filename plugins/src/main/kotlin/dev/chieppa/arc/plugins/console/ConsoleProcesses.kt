package dev.chieppa.arc.plugins.console

import dev.chieppa.arc.plugins.host.LaunchSpec
import dev.chieppa.arc.plugins.host.ProcessLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong

/** The processes Arc runs for plugins, started through [launcher] on [scope] and kept by id until their plugin stops. */
class ConsoleProcesses(private val launcher: ProcessLauncher, private val scope: CoroutineScope) {
    private val ids = AtomicLong()
    private val running = MutableStateFlow<Map<String, ConsoleProcess>>(emptyMap())

    val all: StateFlow<Map<String, ConsoleProcess>> get() = running

    /**
     * Starts [command] for [plugin] in [folder]; what it prints and its end go to [onChunk] and [onExit] too. Throws
     * [java.io.IOException] when it cannot start.
     */
    fun start(plugin: String, command: List<String>, folder: Path, onChunk: (ConsoleProcess, ConsoleChunk) -> Unit, onExit: (ConsoleProcess, Int) -> Unit): ConsoleProcess {
        val id = "$plugin-${ids.incrementAndGet()}"
        val connection = launcher.launch(LaunchSpec(command, folder))
        lateinit var process: ConsoleProcess
        process = ConsoleProcess(id, plugin, command, connection, scope, onChunk = { onChunk(process, it) }, onExit = { onExit(process, it) })
        synchronized(this) { running.value = running.value + (id to process) }
        return process
    }

    operator fun get(id: String): ConsoleProcess? = running.value[id]

    /** Ends and forgets every process [plugin] started. */
    fun stopAll(plugin: String) {
        val mine = running.value.values.filter { it.plugin == plugin }
        mine.forEach { it.kill() }
        synchronized(this) { running.value = running.value - mine.map { it.id }.toSet() }
    }
}
