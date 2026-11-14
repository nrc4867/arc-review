package dev.chieppa.arc.ui.activity

import dev.chieppa.arc.core.activity.ActivityEntry

/** Which entries the Console shows. */
enum class ActivityFilter {
    All,
    Errors,
    ;

    fun shows(entry: ActivityEntry): Boolean = this == All || entry.failed
}
