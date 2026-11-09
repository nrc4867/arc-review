package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_add_repo
import dev.chieppa.stretch.frame.PaletteRow
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.stringResource
import java.nio.file.Path

/** The switcher's one row for a pasted folder: following the repo in [folder], in the error ink when it holds none ([failed]). */
@Composable
fun AddRepoRow(folder: Path, failed: Boolean, highlighted: Boolean, onClick: () -> Unit) {
    val c = Stretch.colors
    val ink = if (failed) c.errorInk else c.inkMuted
    PaletteRow(highlighted, onClick) {
        Icon(ArcIcons.Repo, if (failed) c.errorInk else c.inkControl)
        RowTitle(stringResource(Res.string.switcher_add_repo), "")
        RowRef(folder.toString(), Modifier.weight(1f, fill = false), color = ink)
    }
}
