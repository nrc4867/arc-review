package dev.chieppa.arc.ui.update

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.update_downloading
import dev.chieppa.arc.design.generated.update_downloading_percent
import dev.chieppa.arc.design.generated.update_restart
import dev.chieppa.arc.design.generated.update_to
import dev.chieppa.stretch.frame.StatusItem
import dev.chieppa.stretch.theme.StretchIcons
import org.jetbrains.compose.resources.stringResource

/**
 * The status bar's update item while a newer release is known: "Update to <version>" downloads it, its progress shows
 * while it downloads, and "Restart to update" restarts into it. A failed download keeps the item, marked with a warning.
 */
@Composable
fun UpdateStatusItem(updates: UpdateController) {
    when (val state = updates.state) {
        UpdateState.Current -> Unit
        is UpdateState.Available -> StatusItem(stringResource(Res.string.update_to, state.version), ArcIcons.Download, updates::press)
        is UpdateState.Failed -> StatusItem(stringResource(Res.string.update_to, state.version), StretchIcons.Warn, updates::press)
        is UpdateState.Downloading -> StatusItem(
            state.fraction?.let { stringResource(Res.string.update_downloading_percent, state.version, (it * PERCENT).toInt()) }
                ?: stringResource(Res.string.update_downloading, state.version),
            ArcIcons.Download,
        )
        is UpdateState.Ready -> StatusItem(stringResource(Res.string.update_restart), ArcIcons.Refresh, updates::press)
    }
}

private const val PERCENT = 100
