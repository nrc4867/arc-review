package dev.chieppa.arc.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import dev.chieppa.arc.ui.settings.UiPreferences
import dev.chieppa.arc.ui.workbench.Workbench
import kotlinx.coroutines.flow.drop

/** Hands each change to [workbench]'s look to [save], from the first change on. */
@Composable
fun SavePreferences(workbench: Workbench, save: suspend (UiPreferences) -> Unit) {
    val current by rememberUpdatedState(save)
    LaunchedEffect(workbench) {
        snapshotFlow { workbench.preferences }.drop(1).collect { current(it) }
    }
}
