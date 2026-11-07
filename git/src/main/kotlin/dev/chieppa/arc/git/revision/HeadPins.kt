package dev.chieppa.arc.git.revision

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.git.process.GitException
import dev.chieppa.arc.git.repo.GitRepository

/**
 * Keeps heads in a clone under [RevisionRefNames]: a head the clone lacks is fetched from `origin` by its sha, all in
 * one fetch and one at a time only when that fails, since a host may have dropped some of them.
 */
class HeadPins(private val repo: GitRepository) {
    /** Each of [oids] the clone has or could fetch, pinned, by the oid asked for. */
    suspend fun keep(oids: Collection<String>): Map<String, ObjectId> {
        val found = mutableMapOf<String, ObjectId>()
        val missing = mutableListOf<String>()
        for (oid in oids.distinct()) {
            val commit = repo.resolveCommit(oid)
            if (commit != null) found[oid] = commit else if (FULL_SHA.matches(oid)) missing += oid
        }
        if (missing.isNotEmpty()) {
            if (!fetch(missing)) missing.forEach { fetch(listOf(it)) }
            for (oid in missing) repo.resolveCommit(oid)?.let { found[oid] = it }
        }
        repo.updateRefs(found.values.associateBy(RevisionRefNames::pin))
        return found
    }

    private suspend fun fetch(oids: List<String>): Boolean = try {
        repo.fetch(oids.map { "+$it:${RevisionRefNames.pin(ObjectId(it))}" })
        true
    } catch (_: GitException) {
        false
    }

    private companion object {
        val FULL_SHA = Regex("[0-9a-f]{40}|[0-9a-f]{64}")
    }
}
