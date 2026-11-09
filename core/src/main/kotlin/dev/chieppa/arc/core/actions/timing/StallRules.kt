package dev.chieppa.arc.core.actions.timing

/**
 * When something running counts as stalled: a step running more than [FACTOR] times its usual 90th percentile and at
 * least [MIN_OVER_SECONDS] over it, or a job queued [QUEUE_STALL_SECONDS] when it usually starts within
 * [QUEUE_USUAL_SECONDS]. Nothing with no history is stalled.
 */
object StallRules {
    const val FACTOR = 2L
    const val MIN_OVER_SECONDS = 5 * 60L
    const val QUEUE_STALL_SECONDS = 5 * 60L
    const val QUEUE_USUAL_SECONDS = 60L

    fun step(elapsed: Long, p90: Long?): Boolean = p90 != null && elapsed > FACTOR * p90 && elapsed - p90 >= MIN_OVER_SECONDS

    fun queue(queued: Long, usualP90: Long?): Boolean = usualP90 != null && usualP90 <= QUEUE_USUAL_SECONDS && queued >= QUEUE_STALL_SECONDS
}
