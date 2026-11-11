package dev.chieppa.arc.plugins.manifest

/** The plugin API versions this Arc speaks. A plugin written for another one is not started. */
object ApiVersion {
    const val CURRENT = 1
    val SUPPORTED: Set<Int> = setOf(CURRENT)
}
