package dev.chieppa.arc.plugins.protocol

/** The events Arc sends every running plugin. */
object AppEvents {
    /** A source opened in the strip; the context names it. */
    const val SOURCE_OPENED = "source.opened"

    /** A file was marked or unmarked; the context names the file, `data.kind` the mark or null. */
    const val FILE_MARKED = "file.marked"

    /** A push of a review to its host ended; the context names the source. */
    const val PUSH_FINISHED = "push.finished"

    /** The data key of [FILE_MARKED]'s mark kind. */
    const val KIND = "kind"
}
