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
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_default_name
import dev.chieppa.arc.design.generated.filters_new
import dev.chieppa.arc.design.generated.filters_paste
import dev.chieppa.arc.host.github.search.SearchUrls
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import dev.chieppa.stretch.ui.Divider
import org.jetbrains.compose.resources.stringResource
import dev.chieppa.stretch.ui.TextField

/**
 * Making a saved filter: a pasted GitHub search link becomes one, named for its query, and with the field empty the
 * button makes one on [DEFAULT_QUERY] to edit in its tab. [onCreated] opens the new filter. A link that is not a
 * search is edged in the error color.
 */
@Composable
fun NewFilterSection(watch: PullWatch, onCreated: (String) -> Unit) {
    var link by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    val defaultName = stringResource(Res.string.filter_default_name)
    fun create() {
        val text = link.trim()
        val filter = if (text.isEmpty()) {
            watch.addFilter(defaultName, DEFAULT_QUERY)
        } else {
            val pasted = SearchUrls.parse(text) ?: return run { failed = true }
            watch.addFilter(pasted.name ?: defaultName, pasted.query)
        }
        link = ""
        onCreated(filter.id)
    }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space14, vertical = StretchSpacing.space10),
        verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
    ) {
        TextField(
            link,
            { link = it; failed = false },
            Modifier.fillMaxWidth().height(ArcSizes.filterField).onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) return@onPreviewKeyEvent false
                create()
                true
            },
            placeholder = stringResource(Res.string.filters_paste),
            icon = StretchIcons.Link,
            error = if (failed) "" else null,
        )
        Button(stringResource(Res.string.filters_new), ::create, Modifier.height(ArcSizes.newFilterButton), kind = ButtonKind.Quiet, icon = StretchIcons.Plus)
    }
    Divider()
}

/** The query a filter made without a link starts with. */
const val DEFAULT_QUERY = "is:open"
