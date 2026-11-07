package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.age_short_days
import dev.chieppa.arc.design.generated.age_short_hours
import dev.chieppa.arc.design.generated.age_short_minutes
import dev.chieppa.arc.design.generated.age_short_now
import dev.chieppa.arc.design.generated.age_yesterday
import org.jetbrains.compose.resources.stringResource

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** How long ago [epochMillis] was, short, as a list column shows it: `12 min`, `1 h`, `yesterday`, `4 days`. */
@Composable
fun shortAge(epochMillis: Long, now: Long): String {
    val millis = (now - epochMillis).coerceAtLeast(0)
    return when {
        millis < MINUTE -> stringResource(Res.string.age_short_now)
        millis < HOUR -> stringResource(Res.string.age_short_minutes, (millis / MINUTE).toInt())
        millis < DAY -> stringResource(Res.string.age_short_hours, (millis / HOUR).toInt())
        millis < 2 * DAY -> stringResource(Res.string.age_yesterday)
        else -> stringResource(Res.string.age_short_days, (millis / DAY).toInt())
    }
}
