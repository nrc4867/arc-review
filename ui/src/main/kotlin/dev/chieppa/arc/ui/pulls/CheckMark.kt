package dev.chieppa.arc.ui.pulls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.checks_failed
import dev.chieppa.arc.design.generated.checks_passed
import dev.chieppa.arc.design.generated.checks_running
import dev.chieppa.arc.host.github.pulls.CheckState
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/** A pull request's checks as one icon: passed, failed or running, with a tooltip saying which. Nothing for no checks. */
@Composable
fun CheckMark(state: CheckState, modifier: Modifier = Modifier) {
    val c = Stretch.colors
    val (icon, color, text) = when (state) {
        CheckState.Passed -> Triple(ArcIcons.CheckRun, c.deltaUp, Res.string.checks_passed)
        CheckState.Failed -> Triple(ArcIcons.CheckFail, c.errorInk, Res.string.checks_failed)
        CheckState.Running -> Triple(ArcIcons.CheckPending, c.inkMuted, Res.string.checks_running)
        CheckState.None -> return
    }
    val label = stringResource(text)
    Tooltip(label) { Icon(icon, color, modifier, size = ArcSizes.checkMark, contentDescription = label) }
}
