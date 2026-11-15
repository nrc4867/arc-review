package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.change.ObjectId
import dev.chieppa.arc.core.conversation.PullCommit
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * One commit in a pull request's conversation, on one line: the commit mark, its author's picture and name when
 * [withAuthor], its headline, short sha and how long ago it was committed.
 */
@Composable
fun CommitLine(commit: PullCommit, workbench: Workbench, modifier: Modifier = Modifier, withAuthor: Boolean = false) {
    val c = Stretch.colors
    val small = Stretch.type.bodySm
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
        Icon(ArcIcons.Commit, c.inkMuted, size = ArcSizes.pullIcon)
        if (withAuthor) {
            Avatar(commit.author, commit.avatar, workbench.comments.avatars)
            BasicText(commit.author, style = small.merge(TextStyle(color = c.inkStrong, fontWeight = FontWeight.SemiBold)), maxLines = 1)
        }
        BasicText(commit.headline, Modifier.weight(1f), style = small.merge(TextStyle(color = c.ink)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicText(commit.sha.take(ObjectId.SHORT_LENGTH), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        BasicText(ageText(commit.committedAt), style = small.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
    }
}
