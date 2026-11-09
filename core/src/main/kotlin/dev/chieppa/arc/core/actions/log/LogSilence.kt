package dev.chieppa.arc.core.actions.log

/** A stretch with no output: [seconds] long, ending at the line [afterIndex]. */
data class LogSilence(val afterIndex: Int, val seconds: Long)
