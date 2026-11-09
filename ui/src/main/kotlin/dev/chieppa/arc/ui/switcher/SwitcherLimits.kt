package dev.chieppa.arc.ui.switcher

/** How many results each section shows, and how long the switcher waits on GitHub. */
object SwitcherLimits {
    /** Rows per section when every scope shows. */
    const val SOURCES = 6
    const val FILES = 6
    const val FOLLOWED = 5

    /** Rows per section when one scope shows. */
    const val ONE_SCOPE = 50

    /** How much worse a match in a file's folder ranks than one in its name. */
    const val FOLDER_MATCH = 4

    /** How many of each repo's newest commits the switcher searches by title and sha. */
    const val COMMITS = 300

    /** How long typing must pause before followed repos are asked of GitHub. */
    const val DEBOUNCE_MILLIS = 300L

    /** How long an answer from GitHub or a branch list is reused. */
    const val CACHE_MILLIS = 60_000L
}
