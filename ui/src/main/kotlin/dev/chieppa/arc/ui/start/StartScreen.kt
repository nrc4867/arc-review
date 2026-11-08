package dev.chieppa.arc.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.start.StartContributor
import dev.chieppa.arc.core.start.StartMerge
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Stretch

/**
 * What the pane grid shows while it has no tab: the start screen for the sources selected in the strip. Every
 * contributor in the start registry is asked for the screen's context, Arc's own among them, and their cards are merged
 * with the person's layout into the two columns under the screen's top area and main buttons.
 */
@Composable
fun StartScreen(workbench: Workbench, modifier: Modifier = Modifier) {
    val start = workbench.start
    val context = StartContexts.of(workbench.state)
    val contributors = start.registry.all.map { (id, provider) -> key(id) { StartContributor(id, provider(context)) } }
    val columns = StartMerge.merge(context.kind, contributors, start.layout.of(context.kind), StartCards.slots)
    SideEffect {
        start.kind = context.kind
        start.showing = columns
    }
    StartReads(workbench)
    Box(modifier.fillMaxSize().background(Stretch.colors.surfacePanel).verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = StartSizes.maxWidth + StartSizes.paddingSide * 2)
                .fillMaxWidth()
                .padding(start = StartSizes.paddingSide, end = StartSizes.paddingSide, top = StartSizes.paddingTop, bottom = StartSizes.paddingBottom),
            verticalArrangement = Arrangement.spacedBy(StartSizes.sectionGap),
        ) {
            StartTop(workbench, context.kind, columns.buttons)
            StartColumnsView(workbench, columns)
        }
    }
}
