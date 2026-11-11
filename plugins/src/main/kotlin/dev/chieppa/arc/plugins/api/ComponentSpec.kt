package dev.chieppa.arc.plugins.api

/**
 * A component plugins render, as Arc draws it with a Stretch composable: its [name], which is also the element type
 * sent over the protocol, its [props] and [doc]. [generic] names a type parameter its props take, such as `T` for a
 * list's items.
 */
data class ComponentSpec(val name: String, val doc: String, val props: List<PropSpec>, val generic: String? = null) {
    /** The name of its props interface in the generated types. */
    val propsName: String get() = "${name}Props"
}
