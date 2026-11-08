package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.conversation.StagedRoute
import dev.chieppa.arc.core.conversation.Verdict
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_cancel
import dev.chieppa.arc.design.generated.push_comments
import dev.chieppa.arc.design.generated.push_conversation
import dev.chieppa.arc.design.generated.push_goes_up_as
import dev.chieppa.arc.design.generated.push_in_summary
import dev.chieppa.arc.design.generated.push_line_comments
import dev.chieppa.arc.design.generated.push_replies
import dev.chieppa.arc.design.generated.push_review
import dev.chieppa.arc.design.generated.push_set_aside
import dev.chieppa.arc.design.generated.push_title
import dev.chieppa.arc.design.generated.push_viewed
import dev.chieppa.arc.design.generated.push_viewed_files
import dev.chieppa.arc.design.generated.staged_location
import dev.chieppa.arc.design.generated.staged_separator
import dev.chieppa.arc.design.generated.staged_verdict
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.StretchDialog
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Asks before [source]'s push leaves the machine: the verdict, how many comments go where, the files to be marked
 * viewed, and the summary text as it will go up.
 */
@Composable
fun PushDialog(workbench: Workbench, source: Source) {
    val pusher = workbench.comments.pusher
    val plan = remember(source, workbench.comments.controller.local) { pusher.plan(source) }
    val pull = source is Source.PullRequest
    val close = { pusher.confirming = null }
    val title = stringResource(Res.string.push_title, SourceLabels.of(source))
    StretchDialog(
        title,
        close,
        icon = StretchIcons.Push,
        width = ArcSizes.pushDialog,
        actions = {
            Button(stringResource(Res.string.comment_cancel), close, kind = ButtonKind.Quiet)
            Button(stringResource(if (pull) Res.string.push_review else Res.string.push_comments), { pusher.push(source) }, kind = ButtonKind.Primary, icon = StretchIcons.Push)
        },
    ) {
        val separator = stringResource(Res.string.staged_separator)
        val summary = plan.on(StagedRoute.Summary)
        Column(Modifier.padding(StretchSpacing.space12), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space2)) {
            if (pull) {
                val verdict = plan.summary.verdict
                PushFactRow(if (verdict == Verdict.RequestChanges) ArcIcons.VerdictChanges else ArcIcons.VerdictApprove, stringResource(Res.string.staged_verdict), verdictLabels().getValue(verdict))
            }
            val lines = plan.items.filter { it.route in LINE_ROUTES }
            if (lines.isNotEmpty()) {
                val files = lines.mapNotNull { it.anchor?.path?.substringAfterLast('/') }.distinct().joinToString(", ")
                PushFactRow(ArcIcons.Comment, stringResource(Res.string.push_line_comments), lines.size.toString(), files)
            }
            plan.on(StagedRoute.Reply).takeIf { it.isNotEmpty() }?.let { PushFactRow(ArcIcons.Comment, stringResource(Res.string.push_replies), it.size.toString()) }
            plan.on(StagedRoute.Conversation).takeIf { it.isNotEmpty() }?.let { PushFactRow(ArcIcons.Comment, stringResource(Res.string.push_conversation), it.size.toString()) }
            if (summary.isNotEmpty()) {
                val where = summary.mapNotNull { item -> item.anchor?.let { stringResource(Res.string.staged_location, it.path.substringAfterLast('/'), it.line) } }.joinToString(separator)
                PushFactRow(StretchIcons.Quote, stringResource(Res.string.push_in_summary), summary.size.toString(), where)
            }
            if (pull) {
                val aside = pusher.setAside(source)
                PushFactRow(
                    StretchIcons.Eye,
                    stringResource(Res.string.push_viewed),
                    pluralStringResource(Res.plurals.push_viewed_files, plan.viewed.size, plan.viewed.size),
                    if (aside > 0) pluralStringResource(Res.plurals.push_set_aside, aside, aside) else null,
                )
            }
        }
        val goesUp = listOfNotNull(plan.summary.body.trim().takeIf { it.isNotEmpty() }) +
            summary.mapNotNull { item -> item.anchor?.let { "${stringResource(Res.string.staged_location, it.path.substringAfterLast('/'), it.line)}$separator${item.comment.body}" } }
        if (pull && goesUp.isNotEmpty()) GoesUpAs(goesUp)
    }
}

@Composable
private fun GoesUpAs(paragraphs: List<String>) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    Column(Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space8), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
        BasicText(stringResource(Res.string.push_goes_up_as), style = Stretch.type.eyebrow)
        Column(
            Modifier.fillMaxWidth().background(c.surfaceCanvas, shape).border(Stretch.stroke, c.linePanel, shape).padding(StretchSpacing.space10),
            verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
        ) {
            for (paragraph in paragraphs) BasicText(paragraph, style = Stretch.type.bodySm.merge(TextStyle(color = c.ink, fontWeight = FontWeight.Normal)))
        }
    }
}

/** The routes that go up as comments on lines or files. */
private val LINE_ROUTES = setOf(StagedRoute.Line, StagedRoute.File, StagedRoute.Commit)
