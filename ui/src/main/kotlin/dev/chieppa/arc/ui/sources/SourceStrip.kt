package dev.chieppa.arc.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.mark.FileReviewState
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.sources_add
import dev.chieppa.arc.ui.menus.SourceMenu
import dev.chieppa.arc.ui.review.SourceLoad
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.IconButton
import org.jetbrains.compose.resources.stringResource

/** The strip under the top bar: every source in the review, the active one outlined, and the button to add one. */
@Composable
fun SourceStrip(workbench: Workbench, modifier: Modifier = Modifier) {
    val state = workbench.state
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(ArcSizes.sourceStrip).background(Stretch.colors.surfaceRail).padding(horizontal = StretchSpacing.space8),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (source in state.review.sources) {
                    val load = state.loads[source]
                    SourceChip(
                        source = source,
                        title = load?.ready?.title.orEmpty(),
                        progress = state.progress(source),
                        changedSince = state.states(source).values.count { it == FileReviewState.ChangedSinceReviewed },
                        active = source == state.active,
                        failed = load is SourceLoad.Failed,
                        checks = state.pulls[source]?.checks?.state,
                        onClick = { workbench.activateSource(source) },
                        onRemove = { workbench.removeSource(source) },
                        menu = { close -> SourceMenu(workbench, source, close) },
                    )
                }
            }
            Box {
                IconButton(StretchIcons.Plus, stringResource(Res.string.sources_add), { workbench.addSourceOpen = true }, size = StretchSizes.seg)
                AddSourcePopover(workbench)
            }
        }
        Divider()
    }
}
