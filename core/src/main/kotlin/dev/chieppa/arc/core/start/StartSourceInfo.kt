package dev.chieppa.arc.core.start

/**
 * One selected source as a start screen contributor sees it: its [ref] and [repo], what it is, its [title], [author],
 * [base] and [head] where known, and how many of its files are [reviewed] of [total] once it is read.
 */
data class StartSourceInfo(
    val ref: String,
    val repo: String,
    val kind: StartSourceKind,
    val title: String? = null,
    val author: String? = null,
    val base: String? = null,
    val head: String? = null,
    val reviewed: Int? = null,
    val total: Int? = null,
)
