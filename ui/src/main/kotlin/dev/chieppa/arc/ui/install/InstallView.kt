package dev.chieppa.arc.ui.install

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.install_open
import dev.chieppa.arc.design.generated.install_title
import dev.chieppa.arc.design.generated.install_try_again
import dev.chieppa.arc.install.InstallStep
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Label
import org.jetbrains.compose.resources.stringResource

/**
 * The window Arc shows when it installs itself: its title, one row per step with where it stands, and Open Arc once
 * every step is done, or Try again on a failed one. No board draws it yet.
 */
@Composable
fun InstallView(controller: InstallController, rows: InstallRows, onOpen: () -> Unit) {
    val c = Stretch.colors
    Column(
        Modifier.fillMaxSize().background(c.surfacePanel).padding(InstallSizes.padding),
        verticalArrangement = Arrangement.spacedBy(InstallSizes.gap),
    ) {
        Label(stringResource(Res.string.install_title), Stretch.type.heading, c.inkStrong)
        InstallStep.entries.forEach { step -> InstallStepRow(controller, step, rows.label(step)) }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            if (controller.failed && !controller.running) {
                Button(stringResource(Res.string.install_try_again), controller::start)
            } else {
                Button(stringResource(Res.string.install_open), onOpen, kind = ButtonKind.Primary, enabled = controller.done)
            }
        }
    }
}

/** The install window's measures. */
internal object InstallSizes {
    val padding = 24.dp
    val gap = 12.dp
    val rowGap = 10.dp
    val progressWidth = 160.dp

    /** The most lines a failed step's reason takes. */
    const val reasonLines = 4
}
