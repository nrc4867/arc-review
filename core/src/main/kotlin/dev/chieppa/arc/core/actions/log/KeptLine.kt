package dev.chieppa.arc.core.actions.log

/** A line kept of a job's log: its [index] from 0 in the whole log, the [step] it belongs to, when it was written, and its text. */
data class KeptLine(val index: Int, val step: Int, val epochSecond: Long?, val text: String)
