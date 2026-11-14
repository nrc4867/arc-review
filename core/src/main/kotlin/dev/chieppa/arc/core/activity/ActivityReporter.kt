package dev.chieppa.arc.core.activity

/** Where operations report what they ran, for the person to see in the Console. */
fun interface ActivityReporter {
    fun report(entry: ActivityEntry)

    companion object {
        /** Keeps nothing. */
        val NONE: ActivityReporter = ActivityReporter {}
    }
}
