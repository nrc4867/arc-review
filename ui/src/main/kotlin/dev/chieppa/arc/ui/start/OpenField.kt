package dev.chieppa.arc.ui.start

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.start_find_source
import dev.chieppa.arc.design.generated.start_paste_description
import dev.chieppa.arc.design.generated.start_paste_placeholder
import dev.chieppa.arc.ui.actions.ArcActionIds
import dev.chieppa.arc.ui.switcher.PastedTargets
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource

/**
 * The field a link is pasted into, with Find a source beside it. Enter opens what the field names in a clone here, as
 * the switcher would; anything else goes to the switcher, which offers what it can open or clone.
 */
@Composable
fun OpenField(workbench: Workbench) {
    var text by remember { mutableStateOf("") }
    fun open() {
        val typed = text.trim()
        if (typed.isEmpty()) return
        val source = PastedTargets.read(typed, workbench.gitHubRepos, workbench.state.repos.keys)?.source
        if (source == null || !workbench.openSource(source)) workbench.switcher.show(query = typed)
        text = ""
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            text,
            { text = it },
            Modifier.weight(1f).height(StartSizes.openField).onPreviewKeyEvent { event ->
                (event.type == KeyEventType.KeyDown && event.key == Key.Enter).also { if (it) open() }
            },
            placeholder = stringResource(Res.string.start_paste_placeholder),
            icon = StretchIcons.Link,
            contentDescription = stringResource(Res.string.start_paste_description),
        )
        Button(
            stringResource(Res.string.start_find_source),
            { StartRunner.run(workbench, ArcStartActions.of(ArcActionIds.GO_TO_ANYTHING)) },
            Modifier.height(StartSizes.openField),
            kind = ButtonKind.Primary,
            icon = StretchIcons.Search,
        )
        ActionKeys(workbench, ArcActionIds.GO_TO_ANYTHING.value)
    }
}
