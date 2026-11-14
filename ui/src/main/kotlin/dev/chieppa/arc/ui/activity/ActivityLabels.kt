package dev.chieppa.arc.ui.activity

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.activity_cancelled
import dev.chieppa.arc.design.generated.activity_failed
import dev.chieppa.arc.design.generated.activity_millis
import dev.chieppa.arc.design.generated.activity_seconds
import dev.chieppa.arc.design.generated.activity_started
import dev.chieppa.arc.design.generated.activity_succeeded
import dev.chieppa.arc.design.generated.activity_warned
import org.jetbrains.compose.resources.stringResource

/** The words a Console line uses: each outcome's, and a duration in milliseconds or seconds, the amount at [HOLE]. */
data class ActivityLabels(
    val outcomes: Map<ActivityOutcome, String>,
    val millis: String,
    val seconds: String,
) {
    fun millis(amount: Long): String = millis.replace(HOLE, amount.toString())

    fun seconds(amount: String): String = seconds.replace(HOLE, amount)

    companion object {
        /** Where a label's amount goes. */
        const val HOLE = "{amount}"
    }
}

/** [ActivityLabels] from the strings. */
@Composable
fun activityLabels(): ActivityLabels {
    val outcomes = mapOf(
        ActivityOutcome.Succeeded to stringResource(Res.string.activity_succeeded),
        ActivityOutcome.Failed to stringResource(Res.string.activity_failed),
        ActivityOutcome.Warned to stringResource(Res.string.activity_warned),
        ActivityOutcome.Started to stringResource(Res.string.activity_started),
        ActivityOutcome.Cancelled to stringResource(Res.string.activity_cancelled),
    )
    return ActivityLabels(
        outcomes,
        stringResource(Res.string.activity_millis, ActivityLabels.HOLE),
        stringResource(Res.string.activity_seconds, ActivityLabels.HOLE),
    )
}
