package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.source.Source

/**
 * A comment waiting in the staged batch. [route] says how it will go up. [lineGoneIn] is the short sha of the
 * revision in which its line disappeared, when it did.
 */
data class StagedComment(
    val id: String,
    val source: Source,
    val path: String,
    val line: Int,
    val body: String,
    val route: StagedRoute = StagedRoute.Line,
    val lineGoneIn: String? = null,
)
