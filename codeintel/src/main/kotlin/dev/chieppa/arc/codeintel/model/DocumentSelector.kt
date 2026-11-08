package dev.chieppa.arc.codeintel.model

import kotlinx.serialization.Serializable

/** Which files a provider answers for: those in [languages] (all when empty) whose path ends with one of [suffixes] (any when empty). */
@Serializable
data class DocumentSelector(val languages: Set<String> = emptySet(), val suffixes: Set<String> = emptySet()) {
    fun matches(document: DocumentRef): Boolean =
        (languages.isEmpty() || document.language in languages) && (suffixes.isEmpty() || suffixes.any { document.path.endsWith(it) })

    companion object {
        val All = DocumentSelector()
    }
}
