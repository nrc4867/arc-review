package dev.chieppa.arc.core.conversation

/** What a file looks like now, for placing threads on it: the lines its change touches on the new side, and its length. */
data class ThreadContext(val changedLines: List<Int>, val lineCount: Int)
