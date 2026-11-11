package dev.chieppa.arc.plugins.api

/** One prop of a component, or member of an API interface: its [name], [type], whether [optional], and its [doc]. */
data class PropSpec(val name: String, val type: PropType, val doc: String, val optional: Boolean = false)
