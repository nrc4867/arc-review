package dev.chieppa.arc.host.github.pulls

import dev.chieppa.arc.host.github.api.JsonFields.instant
import dev.chieppa.arc.host.github.api.JsonFields.int
import dev.chieppa.arc.host.github.api.JsonFields.obj
import dev.chieppa.arc.host.github.api.JsonFields.objects
import kotlinx.serialization.json.JsonObject

/** One search's answer: how many pull requests match in all, and the ones that came back. */
data class SearchResult(val total: Int, val pulls: List<PullRequestSummary>)

/**
 * What one poll found: each search's result by key, the rate budget left after it, and why each search that got no
 * answer failed, by key.
 */
data class SearchPoll(val results: Map<String, SearchResult>, val budget: RateBudget?, val failed: Map<String, String> = emptyMap()) {
    companion object {
        /** Reads the `data` of [document] as GitHub answered it. */
        fun parse(document: SearchDocument, data: JsonObject): SearchPoll {
            val results = document.aliases.mapNotNull { (alias, key) ->
                val search = data.obj(alias) ?: return@mapNotNull null
                key to SearchResult(search.int("issueCount") ?: 0, search.objects("nodes").mapNotNull(PullRequestParser::parse))
            }.toMap()
            val rate = data.obj("rateLimit")
            val budget = rate?.let { RateBudget(it.int("limit") ?: 0, it.int("remaining") ?: 0, it.instant("resetAt")) }
            return SearchPoll(results, budget)
        }
    }
}
