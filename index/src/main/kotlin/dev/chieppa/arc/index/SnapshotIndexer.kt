package dev.chieppa.arc.index

import dev.chieppa.arc.core.source.RepoName
import dev.chieppa.arc.git.repo.TreeBlob
import dev.chieppa.arc.index.store.BlobStore
import dev.chieppa.arc.index.store.BlobText
import dev.chieppa.arc.syntax.facts.FactExtractor
import dev.chieppa.arc.syntax.facts.FileFacts
import dev.chieppa.arc.syntax.grammar.Grammar
import dev.chieppa.arc.syntax.grammar.Grammars
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Parses the files of a revision that [cache] has not seen: the blobs stream from [store] in one read while each is
 * parsed on [parse], several at once when it allows. Files with no grammar or over [maxBytes] are left out.
 */
class SnapshotIndexer(
    private val store: BlobStore,
    private val cache: BlobFactsCache,
    private val parse: CoroutineDispatcher,
    private val maxBytes: Long = IndexLimits.MAX_FILE_BYTES,
) {
    suspend fun index(target: IndexTarget): IndexedSnapshot {
        val entries = store.listBlobs(target.repo, target.revision).mapNotNull { entry ->
            val grammar = Grammars.forPath(entry.path) ?: return@mapNotNull null
            if (entry.size > maxBytes) null else entry to grammar
        }
        return IndexedSnapshot(target, files(target.repo, target.revision, entries))
    }

    /** The files of [entries] in [repo] at [revision], parsing those not cached. */
    suspend fun files(repo: RepoName, revision: String, entries: List<Pair<TreeBlob, Grammar>>): List<IndexedFile> {
        val missing = entries.filter { (entry, grammar) -> cache[entry.blob, grammar] == null }
        val grammars = missing.groupBy({ it.first.blob }, { it.second })
        coroutineScope {
            val parses = ArrayList<Deferred<Unit>>()
            store.readBlobs(repo, grammars.keys.toList()) { id, bytes ->
                for (grammar in grammars[id].orEmpty().distinct()) {
                    parses += async(parse) { cache[id, grammar] = extract(grammar, bytes) }
                }
            }
            parses.awaitAll()
        }
        return entries.mapNotNull { (entry, grammar) ->
            cache[entry.blob, grammar]?.let { IndexedFile(repo, revision, entry.path, entry.blob, grammar, it) }
        }
    }

    private fun extract(grammar: Grammar, bytes: ByteArray?): FileFacts {
        val text = bytes?.let(BlobText::decode) ?: return FileFacts.empty(grammar.id)
        return FactExtractor.extract(grammar, text)
    }

    /** One file at a revision, parsed when not cached; null when the revision has no such file or no grammar takes it. */
    suspend fun file(repo: RepoName, revision: String, entry: TreeBlob): IndexedFile? {
        val grammar = Grammars.forPath(entry.path) ?: return null
        return files(repo, revision, listOf(entry to grammar)).firstOrNull()
    }

}
