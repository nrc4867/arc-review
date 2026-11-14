package dev.chieppa.arc.ui.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.chieppa.arc.core.activity.ActivityChannel
import dev.chieppa.arc.core.activity.ActivityLog

/**
 * The Console drawer's state for one window: the [log] it shows, the [channel] chosen (null for all of them), the
 * channels there are to choose, and whether it shows every entry or only failures.
 */
class ActivityConsole(val log: ActivityLog) {
    var filter by mutableStateOf(ActivityFilter.All)

    /** The id of the channel shown, or null for every channel together. */
    var channel by mutableStateOf<String?>(null)

    /** Arc's channels, then each plugin's that has written. */
    var channels by mutableStateOf(ActivityChannel.ARC)
        private set

    /** Counts the times the log was cleared, so the drawer starts again from what is left. */
    var clears by mutableIntStateOf(0)
        private set

    /** Empties the channel shown, or every channel when all are shown. */
    fun clear() {
        log.clear(channel)
        clears++
    }

    /** Takes in the channels the log has now. */
    fun refreshChannels() {
        val known = (ActivityChannel.ARC + log.channels()).distinctBy { it.id }
        if (known != channels) channels = known
    }
}
