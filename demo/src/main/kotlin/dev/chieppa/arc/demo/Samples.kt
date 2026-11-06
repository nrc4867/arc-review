package dev.chieppa.arc.demo

/** The sample conversation's people, words and numbers. Sample data, so it is not in the app's strings. */
internal object Samples {
    const val MFISCHER = "mfischer"
    const val AUTHOR = "r-okafor"
    const val JPARK = "jpark"
    const val DANA = "dana-k"
    const val GUEST = "claude-code"
    const val YOU = "you"

    const val OPEN_ASK = "Should this run in the same transaction as the write above? Two callers could both read the old value first."
    const val OPEN_REPLY = "Fair. The design doc says one transaction too. I will wrap both in the next push."
    const val RESOLVED_ASK = "Can this name say what it holds?"
    const val RESOLVED_DONE = "Renamed."
    const val GUEST_NOTE = "Two callers that both get here with the same value will each pass the check; a lock or a conditional update would serialise them."
    const val ADDRESSED = "This should take the clock as a parameter so tests can move it."
    const val DRAFT = "This is null on rows written before the backfill runs. What happens for those?"
    const val LINK = "Ties to the design doc"
    const val CHANGED_IN = "9c1e2fa"

    const val STAGED_METRIC = "Can we also count this in a metric? On-call will look for a spike, not a log line."
    const val STAGED_INDEX = "The query filters on both columns, so index both."
    const val STAGED_GONE = "This should be a conditional update and return the row count."
    const val STAGED_SUMMARY = "The old constant is still used here."
    const val STAGED_RETRY = "Retry covers a 401 but not a dropped connection."

    const val LINE_51 = 51
    const val LINE_14 = 14
    const val LINE_31 = 31
    const val LINE_22 = 22
    const val LINE_40 = 40

    const val MINUTES_4 = 4 * 60L
    const val HOUR = 3600L
    const val HOURS_2 = 2 * HOUR
    const val HOURS_5 = 5 * HOUR
    const val DAY = 24 * HOUR
}
