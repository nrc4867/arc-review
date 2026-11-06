package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.source.RepoName
import java.nio.file.Path

/** Where a repo's git data is on disk. Reading never depends on how it got there. */
fun interface RepoLocator {
    /** The folder of [repo]'s clone, or null when the repo is not known here. */
    fun locate(repo: RepoName): Path?
}
