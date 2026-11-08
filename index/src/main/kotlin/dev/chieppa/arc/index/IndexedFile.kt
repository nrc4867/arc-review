package dev.chieppa.arc.index

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.syntax.facts.FileFacts
import dev.chieppa.arc.syntax.grammar.Grammar

/** One parsed file: [path] in [repo] at [revision], its [blob], its [grammar] and what the index read from it. */
class IndexedFile(
    val repo: RepoName,
    val revision: String,
    val path: String,
    val blob: ObjectId,
    val grammar: Grammar,
    val facts: FileFacts,
) {
    fun sameFile(other: IndexedFile): Boolean = repo == other.repo && path == other.path
}
