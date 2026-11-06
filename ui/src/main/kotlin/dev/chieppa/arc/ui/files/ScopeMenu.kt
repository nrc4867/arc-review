package dev.chieppa.arc.ui.files

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.core.change.CommitInfo
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.files_scope
import dev.chieppa.arc.design.generated.files_scope_commit
import dev.chieppa.arc.design.generated.files_scope_tooltip
import dev.chieppa.arc.design.generated.files_scope_whole
import dev.chieppa.stretch.frame.Menu
import dev.chieppa.stretch.frame.MenuItem
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The Files drawer's compact scope control: its label says what the tree shows, all the source's changes or one
 * commit's, and pressing it lists the source's [commits] to pick from. With one commit or none there is nothing to
 * pick, so it shows the label alone.
 */
@Composable
fun ScopeMenu(commits: List<CommitInfo>, selected: CommitInfo?, onSelect: (CommitInfo?) -> Unit, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val shape = Stretch.shapes.ctlShape
    var open by remember { mutableStateOf(false) }
    val whole = stringResource(Res.string.files_scope_whole)
    val labels = commits.associateWith { stringResource(Res.string.files_scope_commit, it.id.short(), it.subject) }
    val choosable = commits.size > 1
    val description = stringResource(Res.string.files_scope)
    Box(modifier) {
        Tooltip(stringResource(Res.string.files_scope_tooltip)) {
            Row(
                Modifier
                    .height(StretchSizes.seg)
                    .clip(shape)
                    .background(c.surfaceWell)
                    .border(Stretch.stroke, c.lineControl, shape)
                    .clickable(enabled = choosable, role = Role.DropdownList) { open = true }
                    .semantics { contentDescription = description }
                    .padding(horizontal = StretchSpacing.space6),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (selected == null) ArcIcons.Range else ArcIcons.Commit, c.inkMuted)
                BasicText(
                    selected?.let { labels[it] } ?: whole,
                    Modifier.weight(1f),
                    style = Stretch.type.bodySm.merge(TextStyle(color = c.inkStrong)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (choosable) Icon(StretchIcons.ChevronDown, c.inkMuted)
            }
        }
        Menu(open, { open = false }) {
            MenuItem(whole, { onSelect(null); open = false }, checked = selected == null)
            for (commit in commits) {
                MenuItem(labels.getValue(commit), { onSelect(commit); open = false }, checked = commit == selected)
            }
        }
    }
}
