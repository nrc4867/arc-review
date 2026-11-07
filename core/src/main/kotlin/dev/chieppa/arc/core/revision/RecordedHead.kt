package dev.chieppa.arc.core.revision

import dev.chieppa.arc.core.change.ObjectId

/** A [head] Arc read a source at, the merge-base [base] its change was taken from then, and when it was read [at] (epoch seconds). */
data class RecordedHead(val head: ObjectId, val base: ObjectId, val at: Long)
