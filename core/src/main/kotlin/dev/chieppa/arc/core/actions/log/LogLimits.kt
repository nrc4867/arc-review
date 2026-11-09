package dev.chieppa.arc.core.actions.log

/** How much of a log [LogFilter] keeps. */
data class LogLimits(
    val tail: Int = 20,
    val failedTail: Int = 200,
    val beforeProblem: Int = 5,
    val silences: Int = 3,
    val aroundSilence: Int = 2,
)
