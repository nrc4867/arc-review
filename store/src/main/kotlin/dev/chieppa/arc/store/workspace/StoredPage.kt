package dev.chieppa.arc.store.workspace

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A page, a pane grid tab that shows no file, as written, by kind. */
@Serializable
sealed interface StoredPage {
    val id: String

    /** A saved filter's pull requests, by the filter's id. */
    @Serializable
    @SerialName("filter")
    data class Filter(override val id: String, val filter: String) : StoredPage

    /** A source's revision matrix, by the source's ref. */
    @Serializable
    @SerialName("revisions")
    data class RevisionMatrix(override val id: String, val source: String, val pinned: Boolean = false) : StoredPage
}
