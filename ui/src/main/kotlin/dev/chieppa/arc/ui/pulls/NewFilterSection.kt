package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import dev.chieppa.arc.core.activity.ActivityReporter
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_default_name
import dev.chieppa.arc.design.generated.filters_name
import dev.chieppa.arc.design.generated.filters_new
import dev.chieppa.arc.design.generated.filters_paste
import dev.chieppa.arc.host.github.search.SearchInput
import dev.chieppa.arc.host.github.search.SearchReading
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import dev.chieppa.stretch.ui.TextField
import org.jetbrains.compose.resources.stringResource

/**
 * Making a saved filter: a pasted GitHub search link or a typed search query becomes one, named as the person types
 * in the name field once there is something to name, else for its query. With the field empty the button makes one on
 * [DEFAULT_QUERY] to edit in its tab. [onCreated] opens the new filter. Text that makes no filter is edged in the error
 * color with the reason in its tooltip, and the reason goes to [activity].
 */
@Composable
fun NewFilterSection(watch: PullWatch, activity: ActivityReporter, onCreated: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var refused by remember { mutableStateOf<SearchReading.Refused?>(null) }
    var attempts by remember { mutableStateOf(0) }
    val defaultName = stringResource(Res.string.filter_default_name)
    val reason = refused?.let { searchProblemText(it) }
    LogSearchRefusal(reason?.let { SearchRefusal(input.trim(), it, attempts) }, activity)
    fun create() {
        val text = input.trim()
        val filter = if (text.isEmpty()) {
            watch.addFilter(name.trim().ifEmpty { defaultName }, DEFAULT_QUERY)
        } else {
            when (val reading = SearchInput.read(text)) {
                is SearchReading.Refused -> {
                    refused = reading
                    attempts++
                    return
                }
                is SearchReading.Found -> watch.addFilter(name.trim().ifEmpty { reading.search.name ?: defaultName }, reading.search.query)
            }
        }
        input = ""
        name = ""
        onCreated(filter.id)
    }
    val onEnter = Modifier.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) return@onPreviewKeyEvent false
        create()
        true
    }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
    ) {
        TextField(
            input,
            { input = it; refused = null },
            Modifier.fillMaxWidth().height(ArcSizes.filterField).then(onEnter),
            placeholder = stringResource(Res.string.filters_paste),
            icon = if (SearchInput.isLink(input)) StretchIcons.Link else StretchIcons.Search,
            error = reason,
        )
        if (input.isNotBlank()) {
            TextField(
                name,
                { name = it },
                Modifier.fillMaxWidth().height(ArcSizes.filterField).then(onEnter),
                placeholder = stringResource(Res.string.filters_name),
            )
        }
        Button(stringResource(Res.string.filters_new), ::create, Modifier.height(ArcSizes.newFilterButton), kind = ButtonKind.Quiet, icon = StretchIcons.Plus)
    }
    Divider()
}

/** The query a filter made without a link starts with. */
const val DEFAULT_QUERY = "is:open"
