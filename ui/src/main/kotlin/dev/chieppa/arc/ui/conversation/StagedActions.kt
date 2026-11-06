package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.push_review
import dev.chieppa.arc.design.generated.staged_count
import dev.chieppa.arc.design.generated.verdict_approve
import dev.chieppa.arc.design.generated.verdict_comment
import dev.chieppa.arc.design.generated.verdict_request_changes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Segmented
import org.jetbrains.compose.resources.stringResource

/**
 * The Staged comments drawer's head: how many are staged, the verdict to send with them, and the push button. The
 * button has no action: nothing leaves the machine until a host is connected.
 */
@Composable
fun StagedActions(workbench: Workbench) {
    val staged = stagedComments(workbench)
    if (staged.isEmpty()) return
    val c = Stretch.colors
    val labels = mapOf(
        Verdict.Comment to stringResource(Res.string.verdict_comment),
        Verdict.Approve to stringResource(Res.string.verdict_approve),
        Verdict.RequestChanges to stringResource(Res.string.verdict_request_changes),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
        BasicText(stringResource(Res.string.staged_count, staged.size), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
        Segmented(Verdict.entries, workbench.threadViews.verdict, { workbench.threadViews.verdict = it }, { labels.getValue(it) })
        Button(stringResource(Res.string.push_review), {}, kind = ButtonKind.Primary, icon = StretchIcons.Push)
    }
}
