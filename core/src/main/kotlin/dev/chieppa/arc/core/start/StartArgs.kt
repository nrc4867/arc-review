package dev.chieppa.arc.core.start

/** The names of the arguments a [StartAction] carries for Arc's own actions. */
object StartArgs {
    /** A source's ref, such as `api#412`. */
    const val SOURCE = "source"

    /** A file's path within a source's change. */
    const val PATH = "path"

    /** A web address. */
    const val URL = "url"

    /** A saved filter's id. */
    const val FILTER = "filter"

    /** A start card's id. */
    const val CARD = "card"
}
