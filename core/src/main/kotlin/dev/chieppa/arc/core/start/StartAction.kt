package dev.chieppa.arc.core.start

/** What a click on a start row, button, link or menu item runs: the registered action [id] with its [args]. */
data class StartAction(val id: String, val args: Map<String, String> = emptyMap()) {
    /** The argument named [name], if given. */
    operator fun get(name: String): String? = args[name]
}
