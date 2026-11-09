package dev.chieppa.arc.host.github.actions

import dev.chieppa.arc.host.github.api.ApiResponse
import dev.chieppa.arc.host.github.pulls.RateBudget
import java.time.Instant

/** The REST rate budget as an answer's `x-ratelimit-*` headers report it. */
object RestBudget {
    private const val LIMIT = "x-ratelimit-limit"
    private const val REMAINING = "x-ratelimit-remaining"
    private const val RESET = "x-ratelimit-reset"
    private const val RESOURCE = "x-ratelimit-resource"
    private const val CORE = "core"

    /** The core REST budget [response] reports, or null when it reports none or another resource's. */
    fun of(response: ApiResponse): RateBudget? {
        val resource = response.header(RESOURCE)
        if (resource != null && resource != CORE) return null
        val limit = response.header(LIMIT)?.toIntOrNull() ?: return null
        val remaining = response.header(REMAINING)?.toIntOrNull() ?: return null
        val reset = response.header(RESET)?.toLongOrNull()?.let(Instant::ofEpochSecond)
        return RateBudget(limit, remaining, reset)
    }
}
