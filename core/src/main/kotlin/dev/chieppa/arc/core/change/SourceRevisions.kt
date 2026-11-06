package dev.chieppa.arc.core.change

/**
 * The two commits a source compares: its change is the diff from [base] to [head]. For a branch against a base,
 * [base] is their merge-base, as a pull request shows it.
 */
data class SourceRevisions(val base: ObjectId, val head: ObjectId)
