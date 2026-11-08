package dev.chieppa.arc.ui.start

/** The ids of Arc's own start cards. */
object StartCards {
    const val RECENT = "arc.recent"
    const val REPOSITORIES = "arc.repositories"
    const val KEYS = "arc.keys"
    const val SETTINGS = "arc.settings"
    const val HELP = "arc.help"
    const val WAITING = "arc.waiting"
    const val NOTES = "arc.notes"
    const val REVISIONS = "arc.revisions"
    const val THIS_SOURCE = "arc.this-source"
    const val PROGRESS = "arc.progress"

    /** Symbols one selected source changes and another uses. Reserved: no card shows under it until the index answers that. */
    const val MEET = "arc.meet"

    /** The cards Arc draws with its own composable rather than rows, which show though they hold no rows. */
    val slots: Set<String> = setOf(SETTINGS, PROGRESS)
}
