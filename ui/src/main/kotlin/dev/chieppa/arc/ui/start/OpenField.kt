package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_find_source
import dev.chieppa.arc.design.generated.start_paste_description
import dev.chieppa.arc.design.generated.start_paste_placeholder
import dev.chieppa.arc.ui.switcher.SwitcherBody
import dev.chieppa.arc.ui.switcher.SwitcherCommands
import dev.chieppa.arc.ui.switcher.SwitcherKeys
import dev.chieppa.arc.ui.switcher.SwitcherScope
import dev.chieppa.arc.ui.switcher.SwitcherState
import dev.chieppa.arc.ui.switcher.rememberSwitcherView
import dev.chieppa.arc.ui.switcher.switcherHints
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.frame.SuggestionPanel
import dev.chieppa.stretch.frame.chord
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource

/**
 * The start screen's search field, with Find a source beside it, which puts the cursor in it. While it has the cursor,
 * the switcher's Sources search drops under it: suggestions first, then what the text matches fuzzily, a pasted link's
 * card or a pasted folder's row. Up and Down move through them and Enter opens one, as in the switcher; Esc closes them.
 */
@Composable
fun OpenField(workbench: Workbench) {
    val search = remember { SwitcherState() }
    val focus = remember { FocusRequester() }
    var fieldWidth by remember { mutableIntStateOf(0) }
    var suggestionKeys by remember { mutableStateOf<(KeyEvent) -> Boolean>({ false }) }
    val commands = remember(workbench) {
        SwitcherCommands(
            workbench,
            close = { search.close(); search.type("") },
            onRepoAdded = { search.show(SwitcherScope.Sources, it.value) },
        )
    }
    fun suggest(text: String) = search.show(SwitcherScope.Sources, text)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).onSizeChanged { fieldWidth = it.width }) {
            TextField(
                search.query,
                { if (search.open) search.type(it) else suggest(it) },
                Modifier.fillMaxWidth().height(StartSizes.openField).focusRequester(focus)
                    .onFocusChanged { if (it.isFocused && !search.open) suggest(search.query) }
                    .onPreviewKeyEvent { event ->
                        when {
                            event.type != KeyEventType.KeyDown -> false
                            !search.open -> false
                            event.chord() == SwitcherKeys.CLOSE -> { search.close(); true }
                            else -> suggestionKeys(event)
                        }
                    },
                placeholder = stringResource(Res.string.start_paste_placeholder),
                icon = StretchIcons.Search,
                contentDescription = stringResource(Res.string.start_paste_description),
            )
            if (search.open) {
                val view = rememberSwitcherView(workbench, search)
                val width = with(LocalDensity.current) { fieldWidth.toDp() }
                SideEffect { suggestionKeys = { event -> view.onKey(event, search, commands, scopes = false) } }
                SuggestionPanel(true, search::close, width, hints = switcherHints(view, scopes = false)) {
                    SwitcherBody(workbench, view, search.query, search.index, commands)
                }
            }
        }
        Button(
            stringResource(Res.string.start_find_source),
            {
                focus.requestFocus()
                suggest(search.query)
            },
            Modifier.height(StartSizes.openField),
            kind = ButtonKind.Primary,
            icon = StretchIcons.Search,
        )
    }
}
