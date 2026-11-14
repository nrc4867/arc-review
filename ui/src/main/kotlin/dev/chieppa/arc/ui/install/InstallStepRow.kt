package dev.chieppa.arc.ui.install

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.install_megabytes
import dev.chieppa.arc.install.InstallStep
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.ui.Label
import dev.chieppa.stretch.ui.ProgressBar
import dev.chieppa.stretch.ui.Spinner
import org.jetbrains.compose.resources.stringResource

/**
 * One install step: a check once done, a spinner while it runs, a warning in the error ink when it failed with the
 * reason beneath it, selectable to copy, and for the runtime, its download's progress in megabytes while it arrives.
 */
@Composable
fun InstallStepRow(controller: InstallController, step: InstallStep, label: String) {
    val c = Stretch.colors
    val status = controller.status(step)
    Column(Modifier.fillMaxWidth()) {
        StepLine(controller, step, label, status)
        val reason = controller.reason
        if (status == StepStatus.Failed && reason != null) {
            SelectionContainer(Modifier.padding(start = StretchSizes.chip + InstallSizes.rowGap)) {
                BasicText(
                    reason,
                    Modifier.testTag(InstallTags.reason(step)),
                    style = Stretch.type.bodySm.merge(TextStyle(color = c.errorInk)),
                    maxLines = InstallSizes.reasonLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StepLine(controller: InstallController, step: InstallStep, label: String, status: StepStatus) {
    val c = Stretch.colors
    Row(
        Modifier.fillMaxWidth().height(StretchSizes.row).testTag(InstallTags.row(step, status)),
        horizontalArrangement = Arrangement.spacedBy(InstallSizes.rowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(StretchSizes.chip), contentAlignment = Alignment.Center) {
            when (status) {
                StepStatus.Done -> Icon(StretchIcons.Check, c.ink)
                StepStatus.Running -> Spinner(label)
                StepStatus.Failed -> Icon(StretchIcons.Warn, c.errorInk)
                StepStatus.Waiting -> Unit
            }
        }
        val ink = when (status) {
            StepStatus.Failed -> c.errorInk
            StepStatus.Waiting -> c.inkMuted
            else -> c.ink
        }
        Label(label, Stretch.type.body, ink, Modifier.weight(1f))
        val total = controller.total
        if (step == InstallStep.Runtime && status == StepStatus.Running && total != null && total > 0) {
            ProgressBar(controller.downloaded.toFloat() / total, label, Modifier.width(InstallSizes.progressWidth))
            Label(
                stringResource(Res.string.install_megabytes, (controller.downloaded / MEGABYTE).toInt(), (total / MEGABYTE).toInt()),
                Stretch.type.valueSm,
                c.inkMuted,
            )
        }
    }
}

private const val MEGABYTE = 1024L * 1024L
