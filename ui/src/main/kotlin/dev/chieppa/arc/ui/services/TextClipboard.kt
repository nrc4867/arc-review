package dev.chieppa.arc.ui.services

/** Where copied text goes: the system clipboard in the app, a recorder in tests. */
fun interface TextClipboard {
    fun copy(text: String)
}
