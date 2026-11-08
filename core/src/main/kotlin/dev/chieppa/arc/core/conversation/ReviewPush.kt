package dev.chieppa.arc.core.conversation

import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.source.Source

/**
 * Everything one push sends for [source]: the verdict and summary, each staged comment with its route and where it sits
 * now, and the files to mark viewed on the host ([viewed]). [head] is the commit the comments are on. A source that is
 * not a pull request sends its comments only, on [head].
 */
data class ReviewPush(
    val source: Source,
    val head: ObjectId?,
    val summary: ReviewSummary,
    val items: List<PushItem>,
    val viewed: List<String> = emptyList(),
) {
    fun on(route: StagedRoute): List<PushItem> = items.filter { it.route == route }
}

/** One staged comment as it goes up: by [route], at [anchor] as the source is now. */
data class PushItem(val comment: LocalComment, val route: StagedRoute, val anchor: CommentAnchor? = comment.anchor)

/**
 * What a push did: the staged comments it [sent], those it could not send with why ([failed]), whether the review
 * itself was submitted, and the files marked viewed.
 */
data class PushOutcome(
    val sent: Set<String> = emptySet(),
    val failed: Map<String, String> = emptyMap(),
    val submitted: Boolean = false,
    val viewed: List<String> = emptyList(),
) {
    val complete: Boolean get() = failed.isEmpty() && submitted
}
