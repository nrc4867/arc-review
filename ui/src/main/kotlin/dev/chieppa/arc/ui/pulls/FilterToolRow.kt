package dev.chieppa.arc.ui.pulls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import dev.chieppa.arc.core.watch.FilterGrouping
import dev.chieppa.arc.core.watch.SavedFilter
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.filter_group_label
import dev.chieppa.arc.design.generated.filter_group_none
import dev.chieppa.arc.design.generated.filter_group_repo
import dev.chieppa.arc.design.generated.filter_query
import dev.chieppa.arc.design.generated.filter_refresh
import dev.chieppa.arc.design.generated.filter_updated
import dev.chieppa.arc.ui.conversation.ageText
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.stretch.frame.Dropdown
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSizes
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.IconButton
import dev.chieppa.stretch.ui.TextField
import dev.chieppa.stretch.ui.Tooltip
import org.jetbrains.compose.resources.stringResource

/**
 * The row under a saved filter's tab: its GitHub search query, edited in place and applied with Enter or on leaving
 * the field, how its pull requests are grouped, when it was last polled and a button to poll now. The time is in the
 * error color while polling fails.
 */
@Composable
fun FilterToolRow(filter: SavedFilter, watch: PullWatch, now: Long) {
    val c = Stretch.colors
    var query by remember(filter.id, filter.query) { mutableStateOf(filter.query) }
    fun apply() {
        val text = query.trim()
        if (text.isNotEmpty() && text != filter.query) watch.updateFilter(filter.copy(query = text))
    }
    Row(
        Modifier.fillMaxWidth().height(ArcSizes.filterToolRow).background(c.surfacePanel).padding(start = StretchSpacing.space10, end = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space10),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            query,
            { query = it },
            Modifier.weight(1f)
                .onFocusChanged { if (!it.isFocused) apply() }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown || event.key != Key.Enter) return@onPreviewKeyEvent false
                    apply()
                    true
                },
            icon = StretchIcons.Search,
            contentDescription = stringResource(Res.string.filter_query),
            mono = true,
        )
        val labels = mapOf(
            FilterGrouping.Label to stringResource(Res.string.filter_group_label),
            FilterGrouping.Repo to stringResource(Res.string.filter_group_repo),
            FilterGrouping.None to stringResource(Res.string.filter_group_none),
        )
        Dropdown(
            FilterGrouping.entries,
            filter.grouping,
            { watch.updateFilter(filter.copy(grouping = it)) },
            { labels.getValue(it) },
            labels.getValue(filter.grouping),
            Modifier.width(ArcSizes.groupDropdown),
            icon = ArcIcons.Label,
        )
        val fetched = watch.results[filter.id]?.fetchedAt
        val failing = watch.status == WatchStatus.Failed || watch.status == WatchStatus.Unavailable
        if (fetched != null) {
            val age = ageText(fetched / MILLIS_PER_SECOND, now / MILLIS_PER_SECOND)
            BasicText(
                stringResource(Res.string.filter_updated, age),
                style = Stretch.type.chip.merge(TextStyle(color = if (failing) c.errorInk else c.inkSubtle, fontWeight = FontWeight.Normal)),
                maxLines = 1,
            )
        }
        val refresh = stringResource(Res.string.filter_refresh)
        Tooltip(refresh) { IconButton(ArcIcons.Refresh, refresh, watch::refresh, size = StretchSizes.seg) }
    }
}

private const val MILLIS_PER_SECOND = 1000L
