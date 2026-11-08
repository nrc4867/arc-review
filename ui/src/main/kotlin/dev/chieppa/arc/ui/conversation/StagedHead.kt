package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.staged_summary
import dev.chieppa.arc.design.generated.staged_summary_placeholder
import dev.chieppa.arc.design.generated.staged_verdict
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Segmented
import dev.chieppa.stretch.ui.TextArea
import org.jetbrains.compose.resources.stringResource

/**
 * The top of the Staged comments drawer: the source the batch is for and, for a pull request, the verdict and the
 * summary text that go up with it.
 */
@Composable
fun StagedHead(workbench: Workbench, source: Source) {
    val controller = workbench.comments.controller
    Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space10)) {
        SourceHeading(workbench.state, source)
        if (source !is Source.PullRequest) return@Column
        val summary = controller.summary(source)
        Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
            BasicText(stringResource(Res.string.staged_verdict), style = Stretch.type.eyebrow)
            val labels = verdictLabels()
            Segmented(Verdict.entries, summary.verdict, { controller.setSummary(source, summary.copy(verdict = it)) }, { labels.getValue(it) }, fill = true)
        }
        Column(verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
            BasicText(stringResource(Res.string.staged_summary), style = Stretch.type.eyebrow)
            TextArea(
                summary.body,
                { controller.setSummary(source, summary.copy(body = it)) },
                Modifier.fillMaxWidth(),
                placeholder = stringResource(Res.string.staged_summary_placeholder),
                minLines = SUMMARY_LINES,
            )
        }
    }
}

/** How many lines the summary starts with. */
private const val SUMMARY_LINES = 3
