package dev.chieppa.arc.plugins.host

/**
 * Reads the error a plugin's host gives when its bundle fails to load: the message, and ` @ file:line:column` for the
 * place in the plugin's own files it came from, when the host found one.
 */
object LoadErrors {
    private val PLACE = Regex("""^(.*) @ (.+):(\d+):(\d+)$""", RegexOption.DOT_MATCHES_ALL)

    fun diagnostic(message: String, kind: DiagnosticKind = DiagnosticKind.Load): PluginDiagnostic {
        val match = PLACE.find(message) ?: return PluginDiagnostic(kind, message)
        val (text, file, line, column) = match.destructured
        return PluginDiagnostic(kind, text, file, line.toInt(), column.toInt())
    }
}
