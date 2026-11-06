package dev.chieppa.arc.git

import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.core.source.RepoName

/**
 * Reads files out of a repo's git objects by revision, never checking anything out, so many revisions of many repos
 * can be open at once and the person's own working copies are left alone.
 */
interface GitObjects {
    /** The bytes of [path] in [repo] at [revision], or null when the file does not exist there. */
    suspend fun readFile(repo: RepoName, revision: Revision, path: String): ByteArray?
}
