package dev.chieppa.arc.ui.install

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.install.InstallListener
import dev.chieppa.arc.install.InstallStep
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The install window's state: [install] runs the steps (the runtime, the jar, the launcher), each step's [status]
 * follows as it goes, the runtime's download shows its [downloaded] and [total] bytes, and a failed step stays marked
 * until [start] tries again.
 */
class InstallController(
    private val install: suspend (InstallListener) -> Unit,
    private val scope: CoroutineScope,
    private val dispatchers: ArcDispatchers,
) {
    private val statuses = mutableStateMapOf<InstallStep, StepStatus>()

    var downloaded by mutableStateOf(0L)
        private set
    var total by mutableStateOf<Long?>(null)
        private set

    /** Whether every step is done, so Arc can be opened. */
    val done: Boolean get() = InstallStep.entries.all { status(it) == StepStatus.Done }

    /** Whether a step failed. */
    val failed: Boolean get() = statuses.values.any { it == StepStatus.Failed }

    /** Whether the steps are running now. */
    var running by mutableStateOf(false)
        private set

    fun status(step: InstallStep): StepStatus = statuses[step] ?: StepStatus.Waiting

    /** Runs the steps, from the start; the ones already in place pass at once. */
    fun start() {
        if (running) return
        running = true
        statuses.clear()
        downloaded = 0
        total = null
        var current: InstallStep? = null
        val listener = object : InstallListener {
            override fun started(step: InstallStep) = post {
                current = step
                statuses[step] = StepStatus.Running
            }

            override fun finished(step: InstallStep) = post { statuses[step] = StepStatus.Done }

            override fun runtimeBytes(done: Long, total: Long?) {
                if (total != null && done != total && done - downloaded < PROGRESS_STEP) return
                post {
                    downloaded = done
                    this@InstallController.total = total
                }
            }
        }
        scope.launch {
            val failed = try {
                withContext(dispatchers.io) { install(listener) }
                false
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                true
            }
            // After the listener's changes, which are posted to the same scope.
            post {
                if (failed) statuses[current ?: InstallStep.entries.first { status(it) != StepStatus.Done }] = StepStatus.Failed
                running = false
            }
        }
    }

    private fun post(change: () -> Unit) {
        scope.launch { change() }
    }

    private companion object {
        /** How many bytes of the runtime arrive between updates of its progress. */
        const val PROGRESS_STEP = 512 * 1024L
    }
}
