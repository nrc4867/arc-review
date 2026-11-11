package dev.chieppa.arc.plugins.typecheck

import dev.chieppa.arc.plugins.host.PluginDiagnostic
import java.nio.file.Path

/** Type checks the plugin in a folder from its entry file, giving its type errors; cancelling ends the check. */
fun interface TypeChecker {
    suspend fun check(folder: Path, entry: String): List<PluginDiagnostic>

    companion object {
        /** Finds nothing, where plugins are not type checked. */
        val NONE = TypeChecker { _, _ -> emptyList() }
    }
}
