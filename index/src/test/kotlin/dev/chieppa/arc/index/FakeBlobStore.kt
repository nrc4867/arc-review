package dev.chieppa.arc.index

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.TreeBlob
import dev.chieppa.arc.index.store.BlobStore
import java.security.MessageDigest

/** Repos in memory: each revision a map of path to text, each blob named by its content's SHA-1. Counts blob reads. */
class FakeBlobStore : BlobStore {
    private val trees = HashMap<Pair<RepoName, String>, Map<String, String>>()
    private val blobs = HashMap<ObjectId, ByteArray>()

    /** How many blobs have been read in all. */
    var reads = 0
        private set

    fun put(repo: String, revision: String, files: Map<String, String>) {
        trees[RepoName(repo) to revision] = files
        files.values.forEach { blobs[idOf(it)] = it.toByteArray() }
    }

    override suspend fun listBlobs(repo: RepoName, revision: String): List<TreeBlob> =
        trees.getValue(repo to revision).map { (path, text) -> TreeBlob(path, idOf(text), text.toByteArray().size.toLong()) }

    override suspend fun readBlobs(repo: RepoName, ids: List<ObjectId>, consume: (ObjectId, ByteArray?) -> Unit) {
        for (id in ids) {
            reads++
            consume(id, blobs[id])
        }
    }

    private fun idOf(text: String) = ObjectId(MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) })
}
