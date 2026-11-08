package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.StagedComment
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.push_comments
import dev.chieppa.arc.design.generated.push_review
import dev.chieppa.arc.design.generated.staged_count_conversation
import dev.chieppa.arc.design.generated.staged_count_lines
import dev.chieppa.arc.design.generated.staged_count_replies
import dev.chieppa.arc.design.generated.staged_count_summary
import dev.chieppa.arc.design.generated.staged_count_verdict
import dev.chieppa.arc.design.generated.staged_separator
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The foot of the Staged comments drawer: what the push will send, and the button that asks to send it. */
@Composable
fun StagedFooter(workbench: Workbench, source: Source, staged: List<StagedComment>) {
    val c = Stretch.colors
    val pusher = workbench.comments.pusher
    Column(Modifier.fillMaxWidth().padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8)) {
        val sent = staged.filter { it.lineGoneIn == null }
        val lines = sent.count { it.route == StagedRoute.Line || it.route == StagedRoute.File || it.route == StagedRoute.Commit }
        val parts = listOfNotNull(
            lines.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.staged_count_lines, it, it) },
            sent.count { it.route == StagedRoute.Reply }.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.staged_count_replies, it, it) },
            sent.count { it.route == StagedRoute.Summary }.takeIf { it > 0 }?.let { stringResource(Res.string.staged_count_summary, it) },
            sent.count { it.route == StagedRoute.Conversation }.takeIf { it > 0 }?.let { pluralStringResource(Res.plurals.staged_count_conversation, it, it) },
            stringResource(Res.string.staged_count_verdict).takeIf { source is Source.PullRequest },
        )
        BasicText(parts.joinToString(stringResource(Res.string.staged_separator)), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            val label = stringResource(if (source is Source.PullRequest) Res.string.push_review else Res.string.push_comments)
            Tooltip(label, ArcKeys.PUSH_REVIEW.caps()) {
                Button(
                    label,
                    { if (pusher.canPush(source)) pusher.confirming = source },
                    kind = if (pusher.canPush(source)) ButtonKind.Primary else ButtonKind.Default,
                    icon = StretchIcons.Push,
                )
            }
        }
    }
}
