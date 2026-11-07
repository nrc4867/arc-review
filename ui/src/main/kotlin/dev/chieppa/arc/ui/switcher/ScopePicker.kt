package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_scope
import dev.chieppa.arc.design.generated.switcher_scope_all
import dev.chieppa.arc.design.generated.switcher_scope_files
import dev.chieppa.arc.design.generated.switcher_scope_sources
import dev.chieppa.stretch.ui.Segmented
import org.jetbrains.compose.resources.stringResource

/** What the switcher searches in, picked in its field's row: All, Sources or Files. */
@Composable
fun ScopePicker(scope: SwitcherScope, onPick: (SwitcherScope) -> Unit, modifier: Modifier = Modifier) {
    val labels = mapOf(
        SwitcherScope.All to stringResource(Res.string.switcher_scope_all),
        SwitcherScope.Sources to stringResource(Res.string.switcher_scope_sources),
        SwitcherScope.Files to stringResource(Res.string.switcher_scope_files),
    )
    val description = stringResource(Res.string.switcher_scope)
    Segmented(
        SwitcherScope.entries,
        scope,
        onPick,
        { labels.getValue(it) },
        modifier.semantics { contentDescription = description },
        compact = true,
    )
}
