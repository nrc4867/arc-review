package dev.chieppa.arc.ui.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.ui.util.ArcDispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The window's side of updating Arc through [updates] (none when Arc runs uninstalled, as from Gradle): the newest
 * release found by [check], downloading it on [press], and once it is ready, restarting into it through [quit], which
 * closes the window as Quit does. [switchBack] makes the previous version start instead. A check that fails keeps its
 * reason as [problem] until the next one; [press] with nothing newer known checks again.
 */
class UpdateController(
    private val updates: AppUpdates?,
    private val scope: CoroutineScope,
    private val dispatchers: ArcDispatchers,
    private val quit: () -> Unit,
) {
    var state by mutableStateOf<UpdateState>(UpdateState.Current)
        private set

    /** Why the last check for a newer release failed, or null when it did not. */
    var problem by mutableStateOf<String?>(null)
        private set

    /** Whether a check for a newer release is running now. */
    var checking by mutableStateOf(false)
        private set

    /** Whether Arc can update itself here. */
    val available: Boolean get() = updates != null

    /** The running version, or null when Arc cannot update itself. */
    val version: String? get() = updates?.version

    /** The version Settings can switch back to. */
    val previous: String? get() = updates?.previous

    /**
     * Asks for a newer release, unless one is already on its way or being asked for; a failed check leaves the state as
     * it was and keeps its [problem].
     */
    suspend fun check() {
        val updates = updates ?: return
        if (checking || state is UpdateState.Downloading || state is UpdateState.Ready) return
        checking = true
        val newer = try {
            withContext(dispatchers.io) { updates.check() }
        } catch (e: CancellationException) {
            checking = false
            throw e
        } catch (e: Exception) {
            checking = false
            problem = e.message ?: e.toString()
            return
        }
        checking = false
        problem = null
        state = newer?.let { UpdateState.Available(it) } ?: UpdateState.Current
    }

    /**
     * The status bar item's and Settings' button: checks for a newer release when none is known, downloads an available
     * one, or restarts into a ready one.
     */
    fun press() {
        when (val now = state) {
            UpdateState.Current -> scope.launch { check() }
            is UpdateState.Available -> download(now.version)
            is UpdateState.Failed -> download(now.version)
            is UpdateState.Ready -> restart { it.activate() }
            else -> Unit
        }
    }

    /** Makes the previous version start and restarts into it. */
    fun switchBack() {
        if (updates?.previous == null) return
        restart { it.switchBack() }
    }

    private fun download(version: String) {
        val updates = updates ?: return
        state = UpdateState.Downloading(version, null)
        scope.launch {
            state = try {
                withContext(dispatchers.io) {
                    var shown = -1
                    updates.prepare { done, total ->
                        val percent = total?.takeIf { it > 0 }?.let { (done * PERCENT / it).toInt().coerceIn(0, PERCENT) } ?: return@prepare
                        if (percent == shown) return@prepare
                        shown = percent
                        scope.launch { if (state is UpdateState.Downloading) state = UpdateState.Downloading(version, percent / PERCENT.toFloat()) }
                    }
                }
                UpdateState.Ready(version)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                UpdateState.Failed(version)
            }
        }
    }

    private fun restart(step: suspend (AppUpdates) -> Unit) {
        val updates = updates ?: return
        scope.launch {
            val done = try {
                withContext(dispatchers.io) { step(updates) }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            if (done) quit() else (state as? UpdateState.Ready)?.let { state = UpdateState.Failed(it.version) }
        }
    }

    private companion object {
        const val PERCENT = 100
    }
}
