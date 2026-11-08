package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import dev.chieppa.arc.core.conversation.ConversationEntry
import dev.chieppa.arc.core.conversation.ReviewState
import dev.chieppa.arc.design.ArcIcons
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.review_approved
import dev.chieppa.arc.design.generated.review_changes_requested
import dev.chieppa.arc.design.generated.review_commented
import dev.chieppa.arc.design.generated.review_dismissed
import dev.chieppa.arc.design.generated.review_line_comments
import dev.chieppa.stretch.theme.Stretch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A review summary's verdict chip, in the resolved green for an approval and the warning colors for changes asked, and its comment count. */
@Composable
fun ReviewChips(review: ConversationEntry.Review) {
    val c = Stretch.colors
    when (review.state) {
        ReviewState.Approved -> StateChip(stringResource(Res.string.review_approved), ArcIcons.VerdictApprove, StateLook(Color.Transparent, c.diffAddStrong, c.deltaUp))
        ReviewState.ChangesRequested -> StateChip(stringResource(Res.string.review_changes_requested), ArcIcons.VerdictChanges, StateLook(Color.Transparent, c.warnEdge, c.warnInk))
        ReviewState.Commented -> StateChip(stringResource(Res.string.review_commented), ArcIcons.Comment, StateLook(Color.Transparent, c.chipEdge, c.inkMuted))
        ReviewState.Dismissed -> StateChip(stringResource(Res.string.review_dismissed), ArcIcons.Comment, StateLook(Color.Transparent, c.chipEdge, c.inkSubtle))
    }
    if (review.comments > 0) {
        BasicText(pluralStringResource(Res.plurals.review_line_comments, review.comments, review.comments), style = Stretch.type.bodySm.merge(TextStyle(color = c.inkSubtle)))
    }
}

/** A plain chip with [text], such as the one naming a pull request's author. */
@Composable
fun ChipText(text: String) {
    val c = Stretch.colors
    StateChip(text, null, StateLook(Color.Transparent, c.chipEdge, c.inkControl))
}
