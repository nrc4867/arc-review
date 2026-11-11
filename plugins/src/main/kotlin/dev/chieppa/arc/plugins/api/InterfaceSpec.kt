package dev.chieppa.arc.plugins.api

/** An interface of the `app` object's API: its [name] with any type parameters, its [members] and [doc]. */
data class InterfaceSpec(val name: String, val doc: String, val members: List<PropSpec>)
