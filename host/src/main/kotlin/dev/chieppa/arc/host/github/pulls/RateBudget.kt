package dev.chieppa.arc.host.github.pulls

import java.time.Instant

/** GitHub's GraphQL budget as its `rateLimit` reports it: [remaining] of [limit] points until [resetAt]. */
data class RateBudget(val limit: Int, val remaining: Int, val resetAt: Instant?)
