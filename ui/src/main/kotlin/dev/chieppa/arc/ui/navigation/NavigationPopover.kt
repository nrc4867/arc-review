package dev.chieppa.arc.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.chieppa.arc.codeintel.model.Confidence
import dev.chieppa.arc.codeintel.router.Navigation
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.StretchSpacing

/**
 * The list a Ctrl+click opens under the name in pane [paneId]: a definition's usages by source, or the candidates for a
 * name with several, confident ones first and filtered ones dimmed below. Picking a row goes there.
 */
@Composable
fun NavigationPopover(workbench: Workbench, paneId: String) {
    val navigation = workbench.navigation
    val popup = navigation.popup?.takeIf { it.paneId == paneId }
    Popover(popup != null, navigation::dismiss, width = NavigationSizes.popupWidth, anchor = popup?.anchor) {
        when (val shown = popup?.navigation) {
            is Navigation.Usages -> {
                val answer = shown.answer
                UsagesHead(answer.name, answer.count, answer.repos)
                Column(Modifier.padding(bottom = StretchSpacing.space6)) {
                    for (group in answer.groups) {
                        UsageGroupHeading(workbench, group)
                        group.usages.forEach { TargetRow(it, it.confidence == Confidence.Filtered, navigation::open) }
                    }
                }
                NavigationFooter(answer.groups.map { it.source }.distinct().size)
            }
            is Navigation.Choose -> {
                val answer = shown.answer
                CandidatesHead(answer.name, answer.targets.size)
                Column(Modifier.padding(bottom = StretchSpacing.space6)) {
                    answer.targets.forEach { TargetRow(it, it.confidence == Confidence.Filtered, navigation::open) }
                }
                NavigationFooter(answer.targets.mapNotNull { it.source }.distinct().size)
            }
            else -> Unit
        }
    }
}
