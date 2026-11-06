package dev.chieppa.arc.ui.conversation

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.age_days
import dev.chieppa.arc.design.generated.age_hours
import dev.chieppa.arc.design.generated.age_minutes
import dev.chieppa.arc.design.generated.age_now
import org.jetbrains.compose.resources.stringResource

private const val MINUTE = 60L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/** How long ago [epochSeconds] was, as `4 min ago`, `2 h ago` or `3 d ago`. */
@Composable
fun ageText(epochSeconds: Long, now: Long = System.currentTimeMillis() / 1000): String {
    val seconds = (now - epochSeconds).coerceAtLeast(0)
    return when {
        seconds < MINUTE -> stringResource(Res.string.age_now)
        seconds < HOUR -> stringResource(Res.string.age_minutes, (seconds / MINUTE).toInt())
        seconds < DAY -> stringResource(Res.string.age_hours, (seconds / HOUR).toInt())
        else -> stringResource(Res.string.age_days, (seconds / DAY).toInt())
    }
}
