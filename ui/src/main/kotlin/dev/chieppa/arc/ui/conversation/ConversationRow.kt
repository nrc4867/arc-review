package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.Reaction
import dev.chieppa.arc.core.conversation.TimelineItem
import dev.chieppa.arc.core.source.Source
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.conversation_author
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.Icon
import dev.chieppa.stretch.theme.Stretch
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.PathLabel
import org.jetbrains.compose.resources.stringResource

/**
 * One item of [source]'s conversation: the description, a comment, a review with the threads it started under it, a
 * thread on its own, or a file's heading over its threads, which opens the file.
 */
@Composable
fun ConversationRow(item: TimelineItem, source: Source, workbench: Workbench) {
    val controller = workbench.comments.controller
    val react = { id: String -> { r: Reaction -> controller.react(source, id, r) } }
    val inset = Modifier.padding(horizontal = StretchSpacing.space12, vertical = StretchSpacing.space6)
    when (item) {
        is TimelineItem.Description -> ConversationItem(item.message, item.key, workbench, react(item.message.id), panel = true) {
            ChipText(stringResource(Res.string.conversation_author))
        }
        is TimelineItem.Entry -> when (val entry = item.entry) {
            is ConversationEntry.Comment -> ConversationItem(entry.message, item.key, workbench, react(entry.message.id))
            is ConversationEntry.Review -> ConversationItem(entry.message, item.key, workbench, react(entry.message.id), chips = { ReviewChips(entry) }) {
                for (thread in item.threads) ConversationThread(thread, workbench)
            }
        }
        is TimelineItem.Thread -> ConversationThread(item.thread, workbench, inset)
        is TimelineItem.File -> Column(inset, verticalArrangement = Arrangement.spacedBy(StretchSpacing.space6)) {
            val c = Stretch.colors
            Row(
                Modifier.fillMaxWidth().clickable { workbench.panes.openChange(source, item.path) },
                horizontalArrangement = Arrangement.spacedBy(StretchSpacing.space6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(StretchIcons.File, c.inkMuted)
                PathLabel(item.path, Stretch.type.valueSm, c.inkStrong, Modifier.weight(1f))
            }
            for (thread in item.threads) ConversationThread(thread, workbench, showPath = false)
        }
    }
}
