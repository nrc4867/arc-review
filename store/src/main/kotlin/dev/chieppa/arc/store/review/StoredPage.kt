package dev.chieppa.arc.store.review

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
}
