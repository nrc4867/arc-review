package dev.chieppa.arc.ui.panes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pane_kind_added
import dev.chieppa.arc.design.generated.pane_kind_binary
import dev.chieppa.arc.design.generated.pane_kind_deleted
import dev.chieppa.arc.design.generated.pane_kind_unchanged
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource

/**
 * A chip in the pane's header naming what is unusual about its file: Added in the added colors, Deleted in the removed
 * colors, Unchanged and Binary plain. Nothing for an ordinary change or a whole file.
 */
@Composable
fun FileKindChip(kind: FileKind, binary: Boolean, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val (text, look) = when {
        kind == FileKind.Added -> Res.string.pane_kind_added to Triple(c.diffAddWash, c.diffAddStrong, c.diffAddInk)
        kind == FileKind.Deleted -> Res.string.pane_kind_deleted to Triple(c.diffDelWash, c.diffDelStrong, c.diffDelInk)
        binary -> Res.string.pane_kind_binary to Triple(c.surfaceWell, c.lineControl, c.ink)
        kind == FileKind.Unchanged -> Res.string.pane_kind_unchanged to Triple(c.surfaceWell, c.lineControl, c.ink)
        else -> return
    }
    KindPill(stringResource(text), look.first, look.second, look.third, modifier)
    if (binary && (kind == FileKind.Added || kind == FileKind.Deleted)) {
        KindPill(stringResource(Res.string.pane_kind_binary), c.surfaceWell, c.lineControl, c.ink)
    }
}
