package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.codeintel.model.Precision
import dev.chieppa.arc.codeintel.model.UsageGroup
import dev.chieppa.arc.core.pane.Revision
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.navigation_by_name
import dev.chieppa.arc.design.generated.navigation_exact
import dev.chieppa.arc.design.generated.navigation_group
import dev.chieppa.arc.design.generated.navigation_usages
import dev.chieppa.arc.ui.sources.sourceIcon
import dev.chieppa.arc.ui.util.Revisions
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A usage group's heading: the source and its revision, `4 usages · Kotlin`, and how the group was found. */
@Composable
fun UsageGroupHeading(workbench: Workbench, group: UsageGroup) {
    val c = Stretch.colors
    val source = workbench.state.workspace.sources.firstOrNull { it.ref == group.source }
    Row(
        Modifier.fillMaxWidth().padding(top = StretchSpacing.space10, bottom = StretchSpacing.space4),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(source?.let { sourceIcon(it) } ?: ArcIcons.Repo, c.inkControl)
        BasicText(group.source, style = Stretch.type.valueSm.merge(TextStyle(color = c.inkStrong)), maxLines = 1)
        Row(horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4), verticalAlignment = Alignment.CenterVertically) {
            Icon(ArcIcons.Commit, c.inkMuted, size = NavigationSizes.smallIcon)
            BasicText(Revisions.short(Revision(group.revision)), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        }
        val count = pluralStringResource(Res.plurals.navigation_usages, group.usages.size, group.usages.size)
        BasicText(stringResource(Res.string.navigation_group, count, languageLabel(group.language)), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
        Spacer(Modifier.weight(1f))
        val how = if (group.origin.precision == Precision.Exact) Res.string.navigation_exact else Res.string.navigation_by_name
        BasicText(stringResource(how), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1)
    }
}
