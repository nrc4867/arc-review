package dev.chieppa.arc.ui.revisions

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
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.revisions_compare
import dev.chieppa.arc.design.generated.revisions_files_differ
import dev.chieppa.arc.design.generated.revisions_separator
import dev.chieppa.arc.design.generated.revisions_through_rebase
import dev.chieppa.arc.ui.util.topRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The foot of the Commits drawer while two commits are picked: the two, older first, how many files' changes differ
 * between them, compared through a rebase when their bases differ, and the button that opens those files' changes.
 * [differing] is null until it is worked out.
 */
@Composable
fun CompareSection(comparison: Comparison, differing: Set<String>?, onCompare: () -> Unit) {
    val c = Stretch.colors
    Column(
        Modifier.fillMaxWidth().topRule(c.linePanel).padding(start = StretchSpacing.space14, end = StretchSpacing.space14, top = StretchSpacing.space10, bottom = StretchSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
    ) {
        val sha = Stretch.type.value.merge(TextStyle(color = c.inkStrong))
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6), verticalAlignment = Alignment.CenterVertically) {
            BasicText(comparison.from.head.short(), style = sha)
            Icon(ArcIcons.Compare, c.inkMuted)
            BasicText(comparison.to.head.short(), style = sha)
        }
        if (differing != null) {
            val count = pluralStringResource(Res.plurals.revisions_files_differ, differing.size, differing.size)
            val text = if (comparison.throughRebase) count + stringResource(Res.string.revisions_separator) + stringResource(Res.string.revisions_through_rebase) else count
            BasicText(text, style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)))
        }
        Button(stringResource(Res.string.revisions_compare), onCompare, Modifier.fillMaxWidth(), kind = ButtonKind.Primary, icon = ArcIcons.Compare)
    }
}
