package dev.chieppa.arc.host.github.search

/** A pasted search: the [query] to save and a [name] to start with, null when the caller should pick one. */
data class PastedSearch(val query: String, val name: String?)
