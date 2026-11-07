package dev.chieppa.arc.store.watch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The saved filters and notification rules as written, with when each filter was last looked at, by filter id. */
@Serializable
internal data class StoredWatchlist(
    val filters: List<StoredFilter> = emptyList(),
    val rules: List<StoredRule> = emptyList(),
    val seen: Map<String, Long> = emptyMap(),
)

/** A saved filter as written; [grouping] is a `FilterGrouping` name. */
@Serializable
internal data class StoredFilter(val id: String, val name: String, val query: String, val grouping: String? = null)

/** A notification rule as written, by kind. */
@Serializable
internal sealed interface StoredRule {
    val id: String
    val on: Boolean

    @Serializable
    @SerialName("comments")
    data class Comments(override val id: String, override val on: Boolean = true) : StoredRule

    @Serializable
    @SerialName("mentions")
    data class Mentions(override val id: String, override val on: Boolean = true) : StoredRule

    @Serializable
    @SerialName("filter")
    data class InFilter(override val id: String, val filter: String, override val on: Boolean = true) : StoredRule

    @Serializable
    @SerialName("label")
    data class Labelled(override val id: String, val label: String, val repos: List<String> = emptyList(), override val on: Boolean = true) : StoredRule
}
