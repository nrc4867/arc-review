package dev.chieppa.arc.ui.switcher

import androidx.compose.runtime.Composable
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.switcher_add_repo
import dev.chieppa.arc.design.generated.switcher_hint_beside
import dev.chieppa.arc.design.generated.switcher_hint_close
import dev.chieppa.arc.design.generated.switcher_hint_new_pane
import dev.chieppa.arc.design.generated.switcher_hint_open
import dev.chieppa.arc.design.generated.switcher_hint_scope
import dev.chieppa.arc.design.generated.switcher_hint_switch
import dev.chieppa.stretch.frame.PaletteHint
import org.jetbrains.compose.resources.stringResource

/** The keys a switcher field's footer shows for [view]; Tab's scope only where the field has [scopes]. */
@Composable
fun switcherHints(view: SwitcherView, scopes: Boolean = true): List<PaletteHint> = when {
    view.folder != null -> listOf(
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.SWITCH), stringResource(Res.string.switcher_add_repo)),
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.CLOSE), stringResource(Res.string.switcher_hint_close)),
    )
    view.target == null -> listOfNotNull(
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.SWITCH), stringResource(Res.string.switcher_hint_switch)),
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.NEW_PANE), stringResource(Res.string.switcher_hint_new_pane)),
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.BESIDE), stringResource(Res.string.switcher_hint_beside)),
        if (scopes) PaletteHint(SwitcherKeys.caps(SwitcherKeys.SCOPE), stringResource(Res.string.switcher_hint_scope)) else null,
    )
    else -> listOf(
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.SWITCH), stringResource(Res.string.switcher_hint_open)),
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.NEW_PANE), stringResource(Res.string.switcher_hint_new_pane)),
        PaletteHint(SwitcherKeys.caps(SwitcherKeys.CLOSE), stringResource(Res.string.switcher_hint_close)),
    )
}
