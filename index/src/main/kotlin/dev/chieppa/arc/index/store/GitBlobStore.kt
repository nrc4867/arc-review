package dev.chieppa.arc.index.store

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.BlobPrefetcher
import dev.chieppa.arc.git.repo.GitRepositories
import dev.chieppa.arc.git.repo.TreeBlob

/**
 * [BlobStore] over the repos [repos] knows, through the git CLI. A partial clone first fetches the blobs asked for in
 * one request, so git never fetches them one at a time.
 */
class GitBlobStore(private val repos: GitRepositories) : BlobStore {
    override suspend fun listBlobs(repo: RepoName, revision: String): List<TreeBlob> = repos.repository(repo).listBlobs(revision)

    override suspend fun readBlobs(repo: RepoName, ids: List<ObjectId>, consume: (ObjectId, ByteArray?) -> Unit) {
        val git = repos.repository(repo)
        BlobPrefetcher(git).prefetchBlobs(ids)
        git.readBlobs(ids, consume)
    }
}
