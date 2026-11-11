package dev.chieppa.arc.plugins.host

/** One problem with a plugin, at [file]:[line]:[column] in its folder when it has a place. */
data class PluginDiagnostic(
    val kind: DiagnosticKind,
    val message: String,
    val file: String? = null,
    val line: Int? = null,
    val column: Int? = null,
)
