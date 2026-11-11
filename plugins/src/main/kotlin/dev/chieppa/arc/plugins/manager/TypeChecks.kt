package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.plugins.host.PluginDiagnostic
import dev.chieppa.arc.plugins.typecheck.TypeChecker
import dev.chieppa.arc.plugins.typecheck.TypeErrors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Each plugin's type errors, found by [checker] on [scope] after [waitMillis] of quiet: a new request for a plugin
 * cancels the one before it, waiting or running. A plugin runs whatever its types say.
 */
internal class TypeChecks(private val scope: CoroutineScope, private val checker: TypeChecker, private val waitMillis: Long) {
    private val found = MutableStateFlow<Map<String, TypeErrors>>(emptyMap())
    private val checks = ConcurrentHashMap<String, Job>()

    val errors: StateFlow<Map<String, TypeErrors>> get() = found

    /** Checks plugin [id] in [folder] from [entry] again. */
    fun request(id: String, folder: Path, entry: String) {
        checks.remove(id)?.cancel()
        checks[id] = scope.launch {
            delay(waitMillis)
            val all = checker.check(folder, entry)
            found.update { it + (id to TypeErrors(all.take(SHOWN), all.size)) }
        }
    }

    /** Checks plugin [id] in [folder] from [entry] now, in place of a waiting check, and gives every error found. */
    suspend fun checkNow(id: String, folder: Path, entry: String): List<PluginDiagnostic> {
        checks.remove(id)?.cancel()
        val all = checker.check(folder, entry)
        found.update { it + (id to TypeErrors(all.take(SHOWN), all.size)) }
        return all
    }

    /** Forgets plugin [id]'s errors and stops its check. */
    fun clear(id: String) {
        checks.remove(id)?.cancel()
        found.update { it - id }
    }

    private companion object {
        /** The most errors a plugin's row shows. */
        const val SHOWN = 5
    }
}
