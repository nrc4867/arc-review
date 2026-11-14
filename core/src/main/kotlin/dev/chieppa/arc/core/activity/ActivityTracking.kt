package dev.chieppa.arc.core.activity

import java.time.Instant
import java.util.concurrent.CancellationException

/**
 * Runs [block] and reports it as [title] of [kind] against [place]: how long it took, and when it throws, the
 * failure's message as its error (cancellation as cancelled). What it throws is thrown on.
 */
inline fun <T> ActivityReporter.track(kind: ActivityKind, title: String, place: String? = null, block: () -> T): T {
    val started = Instant.now()
    val start = System.nanoTime()
    try {
        return block().also { report(ActivityEntry(kind, title, ActivityOutcome.Succeeded, started, elapsedMillis(start), place)) }
    } catch (e: CancellationException) {
        report(ActivityEntry(kind, title, ActivityOutcome.Cancelled, started, elapsedMillis(start), place))
        throw e
    } catch (e: Exception) {
        report(ActivityEntry(kind, title, ActivityOutcome.Failed, started, elapsedMillis(start), place, error = failureText(e)))
        throw e
    }
}

/** Milliseconds since [startNanos], a [System.nanoTime] reading. */
fun elapsedMillis(startNanos: Long): Long = (System.nanoTime() - startNanos) / NANOS_PER_MILLI

/** What a failure says: its message, with its causes' messages after it, or its type when it has none. */
fun failureText(error: Throwable): String = generateSequence(error) { it.cause }
    .take(MAX_CAUSES)
    .map { it.message?.takeIf(String::isNotBlank) ?: it::class.java.simpleName }
    .distinct()
    .joinToString("\n")

private const val NANOS_PER_MILLI = 1_000_000L
private const val MAX_CAUSES = 4

/** The line of a failure's [text] that says most: git's last `fatal:` or `error:` line, else its first line with text. */
fun failureHeadline(text: String): String {
    val lines = text.lines().map(String::trim).filter(String::isNotEmpty)
    return lines.lastOrNull { line -> HEADLINE_PREFIXES.any(line::startsWith) } ?: lines.firstOrNull().orEmpty()
}

private val HEADLINE_PREFIXES = listOf("fatal:", "error:")
