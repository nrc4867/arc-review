package dev.chieppa.arc.plugins.api

/**
 * One prop of a component, or member of an API interface: its [name], [type], whether [optional], and its [doc]. A
 * [method] is written as a method signature, its [type] being the signature after the name, so TypeScript compares
 * its parameters both ways: an `App` of one plugin's settings is then an `App`.
 */
data class PropSpec(val name: String, val type: PropType, val doc: String, val optional: Boolean = false, val method: Boolean = false)
