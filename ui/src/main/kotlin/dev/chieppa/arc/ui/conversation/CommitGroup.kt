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
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.conversation.TimelineItem
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_commits_added
import dev.chieppa.arc.design.generated.conversation_commits_show_all
import dev.chieppa.arc.design.generated.conversation_commits_show_fewer
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** How many commits of a run show before Show all. */
private const val COMMITS_SHOWN = 3

/**
 * Commits pushed one after another as one event in the conversation: a lone commit on one line with its author; a
 * run under a line naming who added how many, its first [COMMITS_SHOWN] shown until Show all opens the rest.
 */
@Composable
fun CommitGroup(item: TimelineItem.Commits, workbench: Workbench, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val inset = modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space8)
    val commits = item.commits
    if (commits.size == 1) {
        CommitLine(commits.single(), workbench, inset, withAuthor = true)
        return
    }
    val views = workbench.conversationViews
    val whole = views.showsWhole(item.key)
    val first = commits.first()
    val lineStart = Modifier.padding(start = ArcSizes.avatar + StretchSpacing.space8)
    Column(inset, verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            Avatar(first.author, first.avatar, workbench.comments.avatars)
            BasicText(
                pluralStringResource(Res.plurals.conversation_commits_added, commits.size, first.author, commits.size),
                style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val shown = if (whole) commits else commits.take(COMMITS_SHOWN)
        for (commit in shown) CommitLine(commit, workbench, lineStart)
        if (commits.size > COMMITS_SHOWN) {
            Button(
                if (whole) stringResource(Res.string.conversation_commits_show_fewer) else stringResource(Res.string.conversation_commits_show_all, commits.size),
                { views.toggleWhole(item.key) },
                lineStart,
                kind = ButtonKind.Quiet,
                icon = if (whole) StretchIcons.ChevronDown else StretchIcons.ChevronRight,
            )
        }
    }
}
