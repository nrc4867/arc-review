package dev.chieppa.arc.index

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.index.symbols.SymbolTable

/**
 * The index as of one update: a snapshot per open source, and the [table] of names over every revision they read.
 * Two sources at the same repo and revision share their files.
 */
class IndexState(val snapshots: List<IndexedSnapshot>) {
    private val revisions: List<IndexedSnapshot> = snapshots.distinctBy { it.target.repo to it.target.revision }

    val table: SymbolTable by lazy { SymbolTable(revisions.asSequence().flatMap { it.files }) }

    /** How many files the index holds. */
    val fileCount: Int get() = revisions.sumOf { it.files.size }

    /** Every file the index holds, once each. */
    val files: Sequence<IndexedFile> get() = revisions.asSequence().flatMap { it.files }

    /** [path] in [repo] at [revision], when an open source reads that revision. */
    fun file(repo: RepoName, revision: String, path: String): IndexedFile? =
        revisions.firstOrNull { it.target.repo == repo && it.target.revision == revision }?.file(path)

    /** The first open source that reads [file]'s revision. */
    fun targetOf(file: IndexedFile): IndexTarget? =
        snapshots.firstOrNull { it.target.repo == file.repo && it.target.revision == file.revision }?.target

    companion object {
        val Empty = IndexState(emptyList())
    }
}
