package dev.chieppa.arc.plugins.protocol

/** The levels of [OutputAppendParams]; anything else is taken as [INFO]. */
object OutputLevels {
    const val INFO = "info"
    const val WARN = "warn"
    const val ERROR = "error"

    val ALL: List<String> = listOf(INFO, WARN, ERROR)
}
