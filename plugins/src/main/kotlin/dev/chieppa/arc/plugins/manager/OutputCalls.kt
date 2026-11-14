package dev.chieppa.arc.plugins.manager

import dev.chieppa.arc.core.activity.ActivityChannel
import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityLog
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.plugins.manifest.PluginManifest
import dev.chieppa.arc.plugins.protocol.OutputAppendParams
import dev.chieppa.arc.plugins.protocol.OutputLevels
import java.time.Instant

/**
 * A plugin's output: text it writes to its own channel in the Console, titled with its name, and clearing that
 * channel. It reaches no other channel. Each write is cut to [MAX_TEXT] characters; the log keeps each channel's last
 * entries only.
 */
internal class OutputCalls(private val manifest: () -> PluginManifest, private val log: ActivityLog) {
    private fun channel(): ActivityChannel = manifest().let { ActivityChannel.plugin(it.id, it.title) }

    fun append(params: OutputAppendParams) {
        val text = if (params.text.length > MAX_TEXT) params.text.take(MAX_TEXT) + CUT else params.text
        val outcome = when (params.level) {
            OutputLevels.ERROR -> ActivityOutcome.Failed
            OutputLevels.WARN -> ActivityOutcome.Warned
            else -> ActivityOutcome.Succeeded
        }
        log.report(ActivityEntry(ActivityKind.Output, text, outcome, Instant.now(), channel = channel()))
    }

    fun clear() {
        log.clear(channel().id)
    }

    companion object {
        /** The most characters one write keeps. */
        const val MAX_TEXT = 16_000
        private const val CUT = "…"
    }
}
