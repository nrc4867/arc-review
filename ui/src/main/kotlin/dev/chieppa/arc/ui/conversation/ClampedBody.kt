package dev.chieppa.arc.ui.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.chieppa.arc.design.generated.Res
import dev.chieppa.arc.design.generated.comment_show_less
import dev.chieppa.arc.design.generated.comment_show_more
import dev.chieppa.arc.ui.util.ArcSizes
import dev.chieppa.arc.ui.workbench.Workbench
import dev.chieppa.stretch.theme.StretchIcons
import dev.chieppa.stretch.theme.StretchSpacing
import dev.chieppa.stretch.ui.Button
import dev.chieppa.stretch.ui.ButtonKind
import org.jetbrains.compose.resources.stringResource

/**
 * A comment's [text], formatted, cut short at [ArcSizes.commentClamp] when it is longer, with Show more under it to
 * read it whole and Show less to cut it again. [key] remembers which comments are open whole.
 */
@Composable
fun ClampedBody(key: String, text: String, workbench: Workbench, modifier: Modifier = Modifier) {
    val views = workbench.conversationViews
    val whole = views.showsWhole(key)
    var overflows by remember(key) { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(StretchSpacing.space4)) {
        Box(Modifier.fillMaxWidth().clampHeight(ArcSizes.commentClamp, !whole) { if (overflows != it) overflows = it }) {
            CommentBody(text, workbench)
        }
        if (overflows) {
            Button(
                stringResource(if (whole) Res.string.comment_show_less else Res.string.comment_show_more),
                { views.toggleWhole(key) },
                kind = ButtonKind.Quiet,
                icon = if (whole) StretchIcons.ChevronDown else StretchIcons.ChevronRight,
            )
        }
    }
}
