package dev.chieppa.arc.ui.start

import androidx.compose.runtime.Composable
import dev.chieppa.arc.core.start.StartContext
import dev.chieppa.arc.core.start.StartContribution

/**
 * What one contributor gives the start screen showing [StartContext]. It is composed, so it is asked again whenever
 * what it reads changes: the selection, a source's state, or the contributor's own data.
 */
typealias StartProvider = @Composable (StartContext) -> StartContribution
