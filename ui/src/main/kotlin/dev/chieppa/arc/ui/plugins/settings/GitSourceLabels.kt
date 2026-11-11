package dev.chieppa.arc.ui.plugins.settings

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** How a git source's row writes its parts: its url as a place, a short sha, and when it was last checked. */
object GitSourceLabels {
    /** Between the parts of a source's line. */
    const val SEPARATOR = " · "

    private const val SHORT_SHA = 7
    private const val SCHEME = "://"
    private const val GIT_SUFFIX = ".git"
    private val TIME = DateTimeFormatter.ofPattern("HH:mm")
    private val DAY = DateTimeFormatter.ofPattern("MMM d")

    /** [url] as a place: without its scheme or `.git`, as `github.com/lumen/arc-speckit`. */
    fun place(url: String): String = url.substringAfter(SCHEME).removeSuffix(GIT_SUFFIX).trimEnd('/')

    fun short(sha: String): String = sha.take(SHORT_SHA)

    /** Whether [millis] falls on the same day as [now], here. */
    fun today(millis: Long, now: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean = dateOf(millis, zone) == dateOf(now, zone)

    /** The time of day [millis] falls at, here, as `08:00`. */
    fun time(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String = TIME.format(Instant.ofEpochMilli(millis).atZone(zone))

    /** The day [millis] falls on, here, as `Nov 9`. */
    fun day(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String = DAY.format(Instant.ofEpochMilli(millis).atZone(zone))

    private fun dateOf(millis: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
}
