package dev.chieppa.arc.index

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.TreeBlob
import dev.chieppa.arc.index.store.BlobStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The tree-sitter index of every open source together, which is what lets a name in one repo lead to a definition in
 * another. [update] reads each source's revision straight from its repo's objects; a revision already read is kept,
 * and moving a source to a nearby revision parses only the blobs that changed. Parsing runs on [parse].
 */
class WorkspaceIndex(private val store: BlobStore, parse: CoroutineDispatcher) {
    private val cache = BlobFactsCache()
    private val indexer = SnapshotIndexer(store, cache, parse)
    private val lock = Mutex()
    private val trees = object : LinkedHashMap<Pair<RepoName, String>, List<TreeBlob>>(IndexLimits.LISTED_TREES, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Pair<RepoName, String>, List<TreeBlob>>) = size > IndexLimits.LISTED_TREES
    }
    private val current = MutableStateFlow(IndexState.Empty)
    private val progress = MutableStateFlow(IndexStatus())

    val state: StateFlow<IndexState> = current.asStateFlow()
    val status: StateFlow<IndexStatus> = progress.asStateFlow()

    /** Indexes [targets], keeping what an earlier update read of the same revisions. A source that cannot be read is left out. */
    suspend fun update(targets: List<IndexTarget>) = lock.withLock {
        progress.update { it.copy(indexing = true) }
        try {
            val before = current.value
            val snapshots = targets.mapNotNull { target ->
                val kept = before.snapshots.firstOrNull { it.target.repo == target.repo && it.target.revision == target.revision }
                if (kept != null) IndexedSnapshot(target, kept.files) else readOrSkip(target)
            }
            val next = IndexState(snapshots)
            cache.retain(next.files + before.files)
            current.value = next
            progress.value = IndexStatus(snapshots.size, next.fileCount)
        } finally {
            progress.update { it.copy(indexing = false) }
        }
    }

    /** [path] in [repo] at [revision]: from an indexed source, else read and parsed on its own; null when it is not there. */
    suspend fun file(repo: RepoName, revision: String, path: String): IndexedFile? {
        current.value.file(repo, revision, path)?.let { return it }
        val entry = tree(repo, revision)?.firstOrNull { it.path == path } ?: return null
        return indexer.file(repo, revision, entry)
    }

    private suspend fun tree(repo: RepoName, revision: String): List<TreeBlob>? {
        synchronized(trees) { trees[repo to revision] }?.let { return it }
        val listed = try {
            store.listBlobs(repo, revision)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            return null
        }
        synchronized(trees) { trees[repo to revision] = listed }
        return listed
    }

    private suspend fun readOrSkip(target: IndexTarget): IndexedSnapshot? = try {
        indexer.index(target)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }
}
