package dev.chieppa.arc.plugins.api

/** A documented name in the plugin API, such as a manifest key or an event: its [name], [type] as written, and [doc]. */
data class FieldDoc(val name: String, val type: String, val doc: String)
