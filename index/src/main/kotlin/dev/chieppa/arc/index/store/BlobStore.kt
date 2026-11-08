package dev.chieppa.arc.index.store

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.TreeBlob

/** Where the index reads files from: a repo's tree at a revision and its blobs, never a checkout. */
interface BlobStore {
    /** Every file in [repo]'s tree at [revision]. */
    suspend fun listBlobs(repo: RepoName, revision: String): List<TreeBlob>

    /** The bytes of each of [ids] in [repo], handed to [consume] in order; null for one the repo lacks. */
    suspend fun readBlobs(repo: RepoName, ids: List<ObjectId>, consume: (ObjectId, ByteArray?) -> Unit)
}
