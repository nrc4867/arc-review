package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.chieppa.arc.core.activity.ActivityEntry
import dev.chieppa.arc.core.activity.ActivityKind
import dev.chieppa.arc.core.activity.ActivityOutcome
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filters_refused
import org.jetbrains.compose.resources.stringResource
import java.time.Instant

/** A refused search, as the field showed it: what was typed, why it was refused, and which attempt it was. */
data class SearchRefusal(val text: String, val reason: String, val attempt: Int)

/** Writes each new [refusal] to the GitHub channel of the activity log, as a failure with its reason. */
@Composable
fun LogSearchRefusal(refusal: SearchRefusal?, reporter: ActivityReporter) {
    val title = refusal?.let { stringResource(Res.string.filters_refused, it.text) }
    LaunchedEffect(refusal) {
        if (refusal == null || title == null) return@LaunchedEffect
        reporter.report(ActivityEntry(ActivityKind.GitHub, title, ActivityOutcome.Failed, Instant.now(), error = refusal.reason))
    }
}
