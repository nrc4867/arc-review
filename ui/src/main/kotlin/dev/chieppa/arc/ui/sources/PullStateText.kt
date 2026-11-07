package dev.chieppa.arc.ui.sources

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.pull_state_closed
import dev.chieppa.arc.design.generated.pull_state_merged
import dev.chieppa.arc.host.github.pulls.PullState
import org.jetbrains.compose.resources.stringResource

/** What a merged or closed pull request is called, as its chip's tooltip says it; null while it is open or unknown. */
@Composable
fun pullStateText(state: PullState?): String? = when (state) {
    PullState.Merged -> stringResource(Res.string.pull_state_merged)
    PullState.Closed -> stringResource(Res.string.pull_state_closed)
    PullState.Open, null -> null
}
