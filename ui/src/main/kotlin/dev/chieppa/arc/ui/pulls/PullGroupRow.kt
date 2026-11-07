package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_no_label
import dev.chieppa.arc.host.github.pulls.PullLabel
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.util.bottomRule
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.stringResource

/** A group's head in a saved filter's list: a chevron that folds it, its label or name, and how many it holds. */
@Composable
fun PullGroupRow(group: PullGroup, folded: Boolean, onFold: () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier
            .fillMaxWidth()
            .height(ArcSizes.pullGroup)
            .background(c.surfaceCanvas)
            .bottomRule(c.linePanel)
            .clickable(role = Role.Button, onClick = onFold)
            .padding(horizontal = StretchSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (folded) StretchIcons.ChevronRight else StretchIcons.ChevronDown, c.inkMuted, size = ArcSizes.smallIcon)
        val label = group.label
        when {
            label != null -> LabelPill(label)
            group.name != null -> BasicText(group.name, style = Stretch.type.chip.merge(TextStyle(color = c.inkControl)))
            else -> LabelPill(PullLabel(stringResource(Res.string.filter_no_label), ""))
        }
        BasicText(group.pulls.size.toString(), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)))
    }
}
