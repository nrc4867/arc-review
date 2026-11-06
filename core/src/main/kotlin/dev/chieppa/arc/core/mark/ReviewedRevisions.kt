package dev.chieppa.arc.core.mark

import dev.chieppa.arc.core.change.ObjectId

/** The source's [head] when a file was reviewed, and the [mergeBase] its change was taken from. */
data class ReviewedRevisions(val head: ObjectId, val mergeBase: ObjectId)
