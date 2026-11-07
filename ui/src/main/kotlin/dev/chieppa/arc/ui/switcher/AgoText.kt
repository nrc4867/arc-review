package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.age_days_ago
import dev.chieppa.arc.design.generated.age_hours
import dev.chieppa.arc.design.generated.age_minutes
import dev.chieppa.arc.design.generated.age_now
import dev.chieppa.arc.design.generated.age_yesterday
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val MINUTE = 60L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** How long ago [epochSeconds] was, in words: `now`, `4 min ago`, `2 h ago`, `yesterday`, `3 days ago`. */
@Composable
fun agoText(epochSeconds: Long, nowSeconds: Long): String {
    val seconds = (nowSeconds - epochSeconds).coerceAtLeast(0)
    return when {
        seconds < MINUTE -> stringResource(Res.string.age_now)
        seconds < HOUR -> stringResource(Res.string.age_minutes, (seconds / MINUTE).toInt())
        seconds < DAY -> stringResource(Res.string.age_hours, (seconds / HOUR).toInt())
        seconds < 2 * DAY -> stringResource(Res.string.age_yesterday)
        else -> (seconds / DAY).toInt().let { pluralStringResource(Res.plurals.age_days_ago, it, it) }
    }
}
