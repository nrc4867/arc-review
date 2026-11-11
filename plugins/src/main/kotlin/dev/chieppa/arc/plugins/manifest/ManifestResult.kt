package dev.chieppa.arc.plugins.manifest

/** A manifest read from text: the [Read] value, or why it is [Invalid]. */
sealed interface ManifestResult<out T> {
    data class Read<T>(val value: T) : ManifestResult<T>

    data class Invalid(val reason: String) : ManifestResult<Nothing>
}
