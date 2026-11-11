package dev.chieppa.arc.mcp.dev

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic
import kotlinx.serialization.Serializable

/** A problem with a plugin of [kind], at [file] (in its folder), [line] and [column] where it has a place. */
@Serializable
data class DevProblem(val kind: String, val message: String, val file: String? = null, val line: Int? = null, val column: Int? = null) {
    companion object {
        /** [diagnostic], a type error's column counted from 1 as editors count it. */
        fun of(diagnostic: PluginDiagnostic): DevProblem {
            val column = if (diagnostic.kind == DiagnosticKind.Type && diagnostic.line != null) diagnostic.column?.plus(1) else diagnostic.column
            return DevProblem(diagnostic.kind.name.lowercase(), diagnostic.message, diagnostic.file, diagnostic.line, column)
        }
    }
}
