package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.navigation_definitions
import dev.chieppa.arc.design.generated.navigation_repos
import dev.chieppa.arc.design.generated.navigation_title
import dev.chieppa.arc.design.generated.navigation_title_in
import dev.chieppa.arc.design.generated.navigation_usages
import dev.chieppa.arc.ui.actions.ArcKeys
import dev.chieppa.stretch.frame.caps
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Kbd
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** The usages list's head: `TokenPair · 7 usages in 2 repos`, with the key that shows usages. */
@Composable
fun UsagesHead(name: String, count: Int, repos: Int) {
    val usages = pluralStringResource(Res.plurals.navigation_usages, count, count)
    val title = if (repos > 1) {
        stringResource(Res.string.navigation_title_in, name, usages, pluralStringResource(Res.plurals.navigation_repos, repos, repos))
    } else {
        stringResource(Res.string.navigation_title, name, usages)
    }
    PopoverHeadRow(ArcIcons.Usages, title) { Kbd(ArcKeys.SHOW_USAGES.caps()) }
}

/** The candidates list's head: `refresh · 3 definitions`. */
@Composable
fun CandidatesHead(name: String, count: Int) {
    PopoverHeadRow(ArcIcons.Definition, stringResource(Res.string.navigation_title, name, pluralStringResource(Res.plurals.navigation_definitions, count, count))) {
        Kbd(ArcKeys.GO_TO_DEFINITION.caps())
    }
}

/** A list's head row: its [icon], [title] and what [end] puts at the end. */
@Composable
private fun PopoverHeadRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, end: @Composable () -> Unit) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().padding(vertical = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, c.inkMuted)
        BasicText(title, Modifier.weight(1f), style = Stretch.type.body.merge(TextStyle(color = c.inkStrong)), maxLines = 1, overflow = TextOverflow.Ellipsis)
        end()
    }
}
