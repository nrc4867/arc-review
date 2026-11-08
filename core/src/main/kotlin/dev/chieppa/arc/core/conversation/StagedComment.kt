package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * A comment waiting in the staged batch, placed against the source as it is now. [route] says how it will go up.
 * [path] and [line] are where it sits now, null for a comment on the conversation. [lineGoneIn] is the short sha of the
 * revision in which its line disappeared, when it did; [movedFrom] is the line it was written on, when the source
 * moved it. [fromPending] marks one from a review the person started on the host. [failed] marks one the last push
 * could not send.
 */
data class StagedComment(
    val id: String,
    val source: Source,
    val path: String?,
    val line: Int?,
    val body: String,
    val route: StagedRoute = StagedRoute.Line,
    val lineGoneIn: String? = null,
    val movedFrom: Int? = null,
    val fromPending: Boolean = false,
    val failed: Boolean = false,
)
