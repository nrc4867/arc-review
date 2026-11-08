package dev.chieppa.arc.host.github.review

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps writes to GitHub at least [gapMillis] apart, as GitHub's secondary limits ask. [clock] gives milliseconds and
 * [wait] pauses for that many; tests pass ones that do not sleep.
 */
class MutationPacer(
    private val gapMillis: Long = GAP_MILLIS,
    private val clock: () -> Long = System::currentTimeMillis,
    private val wait: suspend (Long) -> Unit = { delay(it) },
) {
    private val lock = Mutex()
    private var last: Long? = null

    /** Waits until the next write may go. */
    suspend fun pace() = lock.withLock {
        last?.let { previous ->
            val remaining = gapMillis - (clock() - previous)
            if (remaining > 0) wait(remaining)
        }
        last = clock()
    }

    companion object {
        /** The least time between two writes. */
        const val GAP_MILLIS = 1_000L
    }
}
