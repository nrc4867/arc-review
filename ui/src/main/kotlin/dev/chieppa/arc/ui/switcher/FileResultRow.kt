package dev.chieppa.arc.ui.switcher

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.change.ChangedFile
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_added
import dev.chieppa.arc.design.generated.switcher_file_place
import dev.chieppa.arc.design.generated.switcher_removed
import dev.chieppa.arc.ui.util.SourceLabels
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.stringResource

/**
 * A file in the switcher: its name with what [query] matched, the source and folder it is in, and for a [changed]
 * file how it changed and by how many lines.
 */
@Composable
fun FileResultRow(source: Source, path: String, changed: ChangedFile?, query: String, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val folder = path.substringBeforeLast('/', "")
    val ref = SourceLabels.of(source)
    PaletteRow(highlighted, onClick) {
        Icon(StretchIcons.File, c.inkControl)
        RowTitle(path.substringAfterLast('/'), query)
        RowRef(if (folder.isEmpty()) ref else stringResource(Res.string.switcher_file_place, ref, folder))
        Spacer(Modifier.weight(1f))
        if (changed != null) {
            val parts = buildList {
                add(changed.status.letter.toString())
                changed.added?.takeIf { it > 0 }?.let { add(stringResource(Res.string.switcher_added, it)) }
                changed.removed?.takeIf { it > 0 }?.let { add(stringResource(Res.string.switcher_removed, it)) }
            }
            BasicText(parts.joinToString(" "), style = Stretch.type.valueSm.merge(TextStyle(color = c.inkSubtle)), maxLines = 1)
        }
    }
}
