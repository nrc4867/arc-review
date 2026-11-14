package dev.chieppa.arc.ui.activity

import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.stretch.console.ConsoleChunk
import dev.chieppa.stretch.console.ConsoleStream
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * An activity entry as Console text. An operation is one line with its time and place dimmed, its title, how long it
 * took and its outcome; a failure goes on the error stream, with its error text under it, indented. What a plugin wrote
 * is its time and its text, warnings in yellow and errors on the error stream, its channel's title before it when
 * [withChannel]. Every text is shown as plain text: control characters, ANSI codes among them, are taken out.
 */
class ActivityLines(private val labels: ActivityLabels, zone: ZoneId = ZoneId.systemDefault()) {
    private val time = DateTimeFormatter.ofPattern(TIME_PATTERN).withZone(zone)

    fun chunks(entry: ActivityEntry, withChannel: Boolean = false): List<ConsoleChunk> =
        if (entry.kind == ActivityKind.Output) output(entry, withChannel) else operation(entry)

    private fun operation(entry: ActivityEntry): List<ConsoleChunk> {
        val line = buildString {
            append(DIM).append(time.format(entry.started)).append(RESET).append(GAP)
            append(plain(entry.title))
            entry.place?.let { append(GAP).append(DIM).append(plain(it)).append(RESET) }
            entry.millis?.let { append(GAP).append(DIM).append(duration(it)).append(RESET) }
            append(GAP).append(outcome(entry))
            append('\n')
        }
        val error = entry.error?.takeIf { entry.failed }?.let { indented(plain(it)) }
        return listOfNotNull(ConsoleChunk(line, stream(entry)), error?.let { ConsoleChunk(it, ConsoleStream.Err) })
    }

    private fun output(entry: ActivityEntry, withChannel: Boolean): List<ConsoleChunk> {
        val lines = plain(entry.title).trimEnd().lines()
        val text = buildString {
            append(DIM).append(time.format(entry.started)).append(RESET).append(GAP)
            entry.channel.pluginTitle?.takeIf { withChannel }?.let { append(DIM).append(plain(it)).append(RESET).append(GAP) }
            if (entry.outcome == ActivityOutcome.Warned) append(YELLOW)
            append(lines.first())
            if (lines.size > 1) append('\n').append(indented(lines.drop(1).joinToString("\n")).trimEnd('\n'))
            if (entry.outcome == ActivityOutcome.Warned) append(RESET)
            append('\n')
        }
        return listOf(ConsoleChunk(text, stream(entry)))
    }

    private fun stream(entry: ActivityEntry) = if (entry.failed) ConsoleStream.Err else ConsoleStream.Out

    private fun indented(text: String): String = text.trimEnd().lines().joinToString("") { INDENT + it + "\n" }

    private fun outcome(entry: ActivityEntry): String {
        val word = labels.outcomes.getValue(entry.outcome)
        val text = entry.code?.let { "$word ${plain(it)}" } ?: word
        return when (entry.outcome) {
            ActivityOutcome.Failed -> BOLD + text + RESET
            ActivityOutcome.Succeeded -> DIM + text + RESET
            else -> text
        }
    }

    private fun duration(millis: Long): String =
        if (millis < MILLIS_PER_SECOND) labels.millis(millis) else labels.seconds(String.format(Locale.ROOT, SECONDS_FORMAT, millis / MILLIS_PER_SECOND.toDouble()))

    private companion object {
        const val TIME_PATTERN = "HH:mm:ss"
        const val GAP = "  "
        const val INDENT = "    "
        const val DIM = "\u001B[2m"
        const val BOLD = "\u001B[1m"
        const val YELLOW = "\u001B[33m"
        const val RESET = "\u001B[0m"
        const val MILLIS_PER_SECOND = 1000L
        const val SECONDS_FORMAT = "%.1f"
        val CONTROL = Regex("[\u0000-\u0008\u000B-\u001F\u007F-\u009F]")

        /** [text] with its control characters taken out, keeping line breaks and tabs. */
        fun plain(text: String): String = CONTROL.replace(text.replace("\r\n", "\n"), "")
    }
}
