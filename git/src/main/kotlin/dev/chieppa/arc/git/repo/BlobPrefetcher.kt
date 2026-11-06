package dev.chieppa.arc.git.repo

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.parse.NulReader

/**
 * Fetches the blobs a partial clone lacks for whole trees in one request, so nothing later makes git fetch them one at
 * a time. A full clone has every blob already and is left alone.
 */
class BlobPrefetcher(private val repo: GitRepository) {
    /** Fetches every blob in the trees of [revisions] that the clone does not have yet. Returns how many it asked for. */
    suspend fun prefetch(revisions: Collection<ObjectId>): Int {
        if (revisions.isEmpty() || !repo.isPartial()) return 0
        val blobs = revisions.flatMap { blobsOf(it) }.toSet()
        val missing = missing(blobs)
        if (missing.isEmpty()) return 0
        val input = missing.joinToString("\n", postfix = "\n").toByteArray()
        repo.git(FETCH, input)
        return missing.size
    }

    private suspend fun blobsOf(rev: ObjectId): List<String> {
        val reader = NulReader(repo.git(listOf("ls-tree", "-r", "-z", "--full-tree", rev.value)).stdout)
        return buildList {
            while (!reader.atEnd) {
                val entry = reader.field()
                val parts = entry.substringBefore('\t').split(' ')
                if (parts.size == ENTRY_PARTS && parts[1] == BLOB) add(parts[2])
            }
        }
    }

    private suspend fun missing(blobs: Set<String>): List<String> {
        val input = blobs.joinToString("\n", postfix = "\n").toByteArray()
        val output = repo.git(listOf("cat-file", "--batch-check"), input).text
        return output.lineSequence().filter { it.endsWith(MISSING) }.map { it.removeSuffix(MISSING) }.toList()
    }

    private companion object {
        const val ENTRY_PARTS = 3
        const val BLOB = "blob"
        const val MISSING = " missing"
        val FETCH = listOf(
            "-c", "fetch.negotiationAlgorithm=noop", "fetch", "origin", "--no-tags", "--no-write-fetch-head",
            "--recurse-submodules=no", "--filter=blob:none", "--stdin",
        )
    }
}
