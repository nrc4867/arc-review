package dev.chieppa.arc.index

import dev.chieppa.arc.core.source.RepoName

/** One open source as the index reads it: its [source] ref, its [repo] at [revision], and the paths its change touches. */
data class IndexTarget(val source: String, val repo: RepoName, val revision: String, val changedPaths: Set<String> = emptySet())
