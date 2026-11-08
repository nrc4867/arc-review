package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.TimelineItem
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_by_file
import dev.chieppa.arc.design.generated.conversation_fold_all
import dev.chieppa.arc.design.generated.conversation_next
import dev.chieppa.arc.design.generated.conversation_open_as_tab
import dev.chieppa.arc.design.generated.conversation_previous
import dev.chieppa.arc.design.generated.conversation_timeline
import dev.chieppa.arc.design.generated.conversation_unfold_all
import dev.chieppa.arc.ui.util.TipIconButton
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Segmented
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * The row over a conversation: in time order or by file, step to the previous or next item in [list], fold every item
 * to one line or open them all, and, in the drawer, open the conversation as a tab in the grid.
 */
@Composable
fun ConversationToolbar(workbench: Workbench, source: Source, items: List<TimelineItem>, list: LazyListState, inTab: Boolean) {
    val views = workbench.conversationViews
    val scope = rememberCoroutineScope()
    val labels = mapOf(
        ConversationGrouping.Timeline to stringResource(Res.string.conversation_timeline),
        ConversationGrouping.ByFile to stringResource(Res.string.conversation_by_file),
    )
    val step = { by: Int -> scope.launch { list.animateScrollToItem((list.firstVisibleItemIndex + by).coerceIn(0, (list.layoutInfo.totalItemsCount - 1).coerceAtLeast(0))) }; Unit }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space6),
        horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Segmented(ConversationGrouping.entries, views.grouping, { views.grouping = it }, { labels.getValue(it) }, compact = true)
        Spacer(Modifier.weight(1f))
        TipIconButton(StretchIcons.ArrowUp, stringResource(Res.string.conversation_previous), { step(-1) })
        TipIconButton(StretchIcons.ArrowDown, stringResource(Res.string.conversation_next), { step(1) })
        val keys = items.map { it.key }.toSet()
        val allFolded = keys.isNotEmpty() && views.folded.containsAll(keys)
        TipIconButton(
            if (allFolded) ArcIcons.UnfoldAll else ArcIcons.FoldAll,
            stringResource(if (allFolded) Res.string.conversation_unfold_all else Res.string.conversation_fold_all),
            { views.folded = if (allFolded) views.folded - keys else views.folded + keys },
        )
        if (!inTab) TipIconButton(ArcIcons.OpenAsTab, stringResource(Res.string.conversation_open_as_tab), { workbench.panes.openConversation(source) })
    }
}
