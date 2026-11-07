package dev.chieppa.arc.ui.services

/** Where web links open: the system browser in the app, a recorder in tests. */
fun interface LinkOpener {
    fun open(url: String)
}
