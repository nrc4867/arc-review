package dev.chieppa.arc.plugins.bundle

import dev.chieppa.arc.plugins.host.DiagnosticKind
import dev.chieppa.arc.plugins.host.PluginDiagnostic

/** Reads the errors esbuild prints (`✘ [ERROR] message`, then `file:line:column:`) into diagnostics. */
object EsbuildMessages {
    private val ERROR = Regex("""^\S*\s*\[ERROR]\s+(.*)$""")
    private val PLACE = Regex("""^\s+(.+?):(\d+):(\d+):\s*$""")

    fun errors(output: String): List<PluginDiagnostic> {
        val found = mutableListOf<PluginDiagnostic>()
        var message: String? = null
        for (line in output.lines()) {
            val error = ERROR.find(line)
            val place = PLACE.find(line)
            when {
                error != null -> {
                    message?.let { found += PluginDiagnostic(DiagnosticKind.Transpile, it) }
                    message = error.groupValues[1]
                }
                place != null && message != null -> {
                    val (file, row, column) = place.destructured
                    found += PluginDiagnostic(DiagnosticKind.Transpile, message, file.replace('\\', '/'), row.toInt(), column.toInt())
                    message = null
                }
            }
        }
        message?.let { found += PluginDiagnostic(DiagnosticKind.Transpile, it) }
        return found
    }
}
