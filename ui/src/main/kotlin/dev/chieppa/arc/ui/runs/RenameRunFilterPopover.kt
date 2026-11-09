package dev.chieppa.arc.ui.runs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import dev.chieppa.arc.core.actions.RunFilter
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_name
import dev.chieppa.arc.design.generated.filter_rename_title
import dev.chieppa.arc.design.generated.filter_save
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.Popover
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource

/** Renaming run filter [filter]: its name in a field, saved with the button or Enter. */
@Composable
fun RenameRunFilterPopover(filter: RunFilter?, watch: RunWatch, onDismiss: () -> Unit) {
    var name by remember(filter?.id) { mutableStateOf(filter?.name.orEmpty()) }
    fun save() {
        val target = filter ?: return
        if (name.isBlank()) return
        watch.updateFilter(target.copy(name = name.trim()))
        onDismiss()
    }
    Popover(filter != null, onDismiss, title = stringResource(Res.string.filter_rename_title), width = ArcSizes.popoverWidth) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = StretchSpacing.space4),
            horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                name,
                { name = it },
                Modifier.weight(1f).onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) return@onPreviewKeyEvent false
                    save()
                    true
                },
                placeholder = stringResource(Res.string.filter_name),
                error = if (name.isBlank()) "" else null,
            )
            Button(stringResource(Res.string.filter_save), ::save, kind = ButtonKind.Primary)
        }
    }
}
