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
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.navigation_footer
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.pluralStringResource

/** Where a list's answers came from: the tree-sitter index, names matched across [sources] sources. */
@Composable
fun NavigationFooter(sources: Int) {
    val c = Stretch.colors
    Divider()
    Row(
        Modifier.fillMaxWidth().padding(vertical = StretchSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(StretchIcons.Info, c.inkMuted, size = NavigationSizes.smallIcon)
        BasicText(pluralStringResource(Res.plurals.navigation_footer, sources, sources), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkMuted)), maxLines = 1)
    }
}
