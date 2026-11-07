package dev.chieppa.arc.ui.revisions

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.matrix_today
import org.jetbrains.compose.resources.stringResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val CLOCK = DateTimeFormatter.ofPattern("HH:mm")
private val WEEKDAY = DateTimeFormatter.ofPattern("EEE HH:mm")
private val DATE = DateTimeFormatter.ofPattern("MMM d")
private const val WEEK_DAYS = 6L

/**
 * When a revision became the head, [epochSeconds], as its column shows it: `today 08:47`, `Mon 09:12` within the
 * week, else `Nov 3`, in [zone] as of [nowSeconds].
 */
@Composable
fun revisionTime(epochSeconds: Long, nowSeconds: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val at = Instant.ofEpochSecond(epochSeconds).atZone(zone)
    val days = ChronoUnit.DAYS.between(at.toLocalDate(), Instant.ofEpochSecond(nowSeconds).atZone(zone).toLocalDate())
    return when {
        days == 0L -> stringResource(Res.string.matrix_today, CLOCK.format(at))
        days in 1..WEEK_DAYS -> WEEKDAY.format(at)
        else -> DATE.format(at)
    }
}
