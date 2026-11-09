package dev.chieppa.arc.core.actions.log

import java.time.Instant
import java.time.format.DateTimeParseException

/** Reading a line of a GitHub job log: the timestamp it starts with, and whether it reports an error or a warning. */
object LogLineText {
    private val STAMP = Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z) ?")
    private val PROBLEM = Regex(
        "##\\[(error|warning)]|^\\s*(error|warning|fatal|e|w)(\\s*:|\\[)|\\b(error|warning):|^FAILURE:|\\bFAILED\\b",
        RegexOption.IGNORE_CASE,
    )

    /** The line's time in epoch seconds and its text after the timestamp; a line without one has a null time. */
    fun split(raw: String): Pair<Long?, String> {
        val line = raw.removePrefix(BOM)
        val match = STAMP.find(line) ?: return null to line
        val at = try {
            Instant.parse(match.groupValues[1]).epochSecond
        } catch (_: DateTimeParseException) {
            null
        }
        return at to line.substring(match.range.last + 1)
    }

    fun isProblem(text: String): Boolean = PROBLEM.containsMatchIn(text)

    private const val BOM = "﻿"
}
