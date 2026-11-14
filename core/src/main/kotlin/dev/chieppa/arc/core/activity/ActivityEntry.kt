package dev.chieppa.arc.core.activity

import java.time.Instant

/**
 * One operation Arc ran: its [title] (a command line or a request), the [place] it ran against (a folder or a host),
 * when it [started], how long it took in [millis] (null for one still running), its [outcome], a short [code] such as
 * an exit code or an HTTP status, and for a failure the [error] text the operation gave. It shows in [channel], by
 * default its kind's. [id] orders entries; the log gives it.
 */
data class ActivityEntry(
    val kind: ActivityKind,
    val title: String,
    val outcome: ActivityOutcome,
    val started: Instant,
    val millis: Long? = null,
    val place: String? = null,
    val code: String? = null,
    val error: String? = null,
    val channel: ActivityChannel = ActivityChannel.of(kind),
    val id: Long = 0,
) {
    val failed: Boolean get() = outcome == ActivityOutcome.Failed
}
