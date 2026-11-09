package dev.chieppa.arc.host.github.actions

/** One conditional read: what came back, or null when it is unchanged since [etag]. */
data class Conditional<T>(val value: T?, val etag: String?) {
    val unchanged: Boolean get() = value == null
}
