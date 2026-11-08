package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** The usages of [name], grouped by source and language, in the order the request listed its sources. */
@Serializable
data class UsagesAnswer(val name: String, val groups: List<UsageGroup>) {
    /** How many usages there are in all. */
    val count: Int get() = groups.sumOf { it.usages.size }

    /** How many repos the usages are in. */
    val repos: Int get() = groups.flatMap { group -> group.usages.map { it.location.document.repo } }.distinct().size
}
